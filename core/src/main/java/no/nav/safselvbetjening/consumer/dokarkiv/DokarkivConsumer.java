package no.nav.safselvbetjening.consumer.dokarkiv;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.CallIdExchangeFilterFunction;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalposter;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.FinnJournalposterRequest;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Set;

import static java.lang.String.format;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static no.nav.safselvbetjening.azure.AzureProperties.CLIENT_REGISTRATION_DOKARKIV;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class DokarkivConsumer {
	// resilience4j instanser (se application.properties)
	private static final String DOKARKIV_METADATA = "dokarkivmetadata";
	private static final String DOKARKIV_DOKUMENTOVERSIKT = "dokarkivdokumentoversikt";
	private static final String DOKARKIV_HENTDOKUMENT = "dokarkivhentdokument";
	private final WebClient webClient;
	private final CircuitBreaker dokarkivDokumentoversiktCircuitBreaker;
	private final CircuitBreaker dokarkivHentdokumentCircuitBreaker;
	private final CircuitBreaker dokarkivMetadataCircuitBreaker;
	private final Retry dokarkivDokumentoversiktRetry;
	private final Retry dokarkivHentdokumentRetry;
	private final Retry dokarkivMetadataRetry;

	public DokarkivConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final CodecProperties codecProperties,
							final WebClient webClient,
							final CircuitBreakerRegistry circuitBreakerRegistry,
							final RetryRegistry retryRegistry) {
		SafSelvbetjeningProperties.AzureEndpoint dokarkiv = safSelvbetjeningProperties.getEndpoints().getDokarkiv();
		this.webClient = webClient.mutate()
				.baseUrl(dokarkiv.getUrl())
				.filter(new CallIdExchangeFilterFunction(NAV_CALLID))
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(clientCodecConfigurer ->
								clientCodecConfigurer.defaultCodecs()
										.maxInMemorySize((int) codecProperties.getMaxInMemorySize().toBytes())
						)
						.build())
				.build();
		this.dokarkivDokumentoversiktCircuitBreaker = circuitBreakerRegistry.circuitBreaker(DOKARKIV_DOKUMENTOVERSIKT);
		this.dokarkivHentdokumentCircuitBreaker = circuitBreakerRegistry.circuitBreaker(DOKARKIV_HENTDOKUMENT);
		this.dokarkivMetadataCircuitBreaker = circuitBreakerRegistry.circuitBreaker(DOKARKIV_METADATA);
		this.dokarkivDokumentoversiktRetry = retryRegistry.retry(DOKARKIV_DOKUMENTOVERSIKT);
		this.dokarkivHentdokumentRetry = retryRegistry.retry(DOKARKIV_HENTDOKUMENT);
		this.dokarkivMetadataRetry = retryRegistry.retry(DOKARKIV_METADATA);
	}

	public Mono<ArkivJournalposter> finnJournalposter(FinnJournalposterRequest request, Set<String> fields) {
		return webClient.post()
				.uri(uriBuilder -> {
					uriBuilder.path("/finnjournalposter");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build();
				})
				.bodyValue(request)
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalposter.class)
				.onErrorMap(error -> mapFinnJournalposterError(error, request))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivDokumentoversiktCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivDokumentoversiktRetry));
	}

	private Throwable mapFinnJournalposterError(Throwable error, FinnJournalposterRequest request) {
		if (error instanceof WebClientResponseException webException) {
			if (webException.getStatusCode().is4xxClientError()) {
				return new ConsumerFunctionalException(format("finnJournalposter feilet funksjonelt. status=%s, request=%s. Feilmelding=%s",
						webException.getStatusCode(), request, webException.getMessage()));
			} else {
				return new ConsumerTechnicalException(format("finnJournalposter feilet teknisk. status=%s, request=%s. Feilmelding=%s",
						webException.getStatusCode(), request, webException.getMessage()), webException);
			}
		} else {
			return new ConsumerTechnicalException("finnJournalposter feilet med ukjent teknisk feil", error);
		}
	}

	public ArkivJournalpost journalpost(String journalpostId, String dokumentInfoId, Set<String> fields) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}", "dokumentInfoId", "{dokumentInfoId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(journalpostId, dokumentInfoId);
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalpost.class)
				.onErrorMap(error -> mapHentJournalpostError(error, journalpostId, dokumentInfoId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Throwable mapHentJournalpostError(Throwable error, String journalpostId, String dokumentInfoId) {
		if (error instanceof WebClientResponseException.NotFound notFound) {
			return new JournalpostIkkeFunnetException(format("Journalpost med journalpostId=%s, dokumentInfoId=%s ikke funnet i Joark.",
					journalpostId, dokumentInfoId), notFound);
		}
		if (error instanceof WebClientResponseException webException) {
			if (webException.getStatusCode().is4xxClientError()) {
				return new ConsumerFunctionalException(format("hentJournalpost feilet funksjonelt. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
						webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()));
			} else {
				return new ConsumerTechnicalException(format("hentJournalpost feilet teknisk. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
						webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()), webException);
			}
		}
		return new ConsumerTechnicalException(format("hentJournalpost feilet med ukjent teknisk feil. journalpostId=%s, dokumentInfoId=%s",
				journalpostId, dokumentInfoId), error);
	}

	public ArkivJournalpost journalpost(long journalpostId, Set<String> fields) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(journalpostId);
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalpost.class)
				.onErrorMap(error -> handleErrorJournalpost(error, journalpostId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Throwable handleErrorJournalpost(Throwable error, long journalpostId) {
		if (error instanceof WebClientResponseException.NotFound notFound) {
			return new JournalpostIkkeFunnetException(format("Journalpost med journalpostId=%d ikke funnet i Joark.", journalpostId), notFound);
		}
		if (error instanceof WebClientResponseException webException) {
			if (webException.getStatusCode().is4xxClientError()) {
				return new ConsumerFunctionalException(format("hentJournalpost feilet funksjonelt. status=%s, journalpostId=%d. Feilmelding=%s",
						webException.getStatusCode(), journalpostId, webException.getMessage()));
			} else {
				return new ConsumerTechnicalException(format("hentJournalpost feilet teknisk. status=%s, journalpostId=%d. Feilmelding=%s",
						webException.getStatusCode(), journalpostId, webException.getMessage()), webException);
			}
		}
		return new ConsumerTechnicalException(format("hentJournalpost feilet med ukjent teknisk feil. journalpostId=%d", journalpostId), error);
	}

	public HentDokumentResponseTo hentDokument(final String dokumentInfoId, final String variantFormat) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/hentdokument/{dokumentInfoId}/{variantFormat}")
						.build(dokumentInfoId, variantFormat))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_PDF)
				.exchangeToMono(clientResponse -> {
					if (clientResponse.statusCode().is2xxSuccessful()) {
						return clientResponse.bodyToMono(byte[].class)
								.map(responseBytes -> HentDokumentResponseTo.builder()
										.dokument(responseBytes)
										.mediaType(clientResponse.headers().asHttpHeaders().getContentType())
										.build());
					} else {
						return clientResponse.createError();
					}
				})
				.onErrorMap(error -> handleErrorHentDokument(error, dokumentInfoId, variantFormat))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivHentdokumentCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivHentdokumentRetry))
				.block();
	}

	private Throwable handleErrorHentDokument(Throwable error, String dokumentInfoId, String variantFormat) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			if (error instanceof WebClientResponseException.NotFound) {
				return new DokumentIkkeFunnetException("Fant ikke dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, error);
			}
			return new ConsumerFunctionalException("Funksjonell feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, error);
		} else {
			return new ConsumerTechnicalException("Teknisk feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, error);
		}
	}
}
