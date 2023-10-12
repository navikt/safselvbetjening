package no.nav.safselvbetjening.consumer.dokarkiv;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.CallIdExchangeFilterFunction;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.lang.String.format;
import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static no.nav.safselvbetjening.azure.AzureProperties.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.safselvbetjening.azure.AzureProperties.getOAuth2AuthorizeRequestForAzure;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Slf4j
@Component
public class DokarkivConsumer {
	// resilience4j instanser (se application.properties)
	private static final String DOKARKIV_METADATA = "dokarkivmetadata";
	private static final String DOKARKIV_DOKUMENTOVERSIKT = "dokarkivdokumentoversikt";
	private static final String DOKARKIV_HENTDOKUMENT = "dokarkivhentdokument";
	private final RestTemplate restTemplate;
	private final WebClient webClient;
	private final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

	public DokarkivConsumer(final RestTemplateBuilder restTemplateBuilder,
							final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final CodecProperties codecProperties,
							final ClientHttpRequestFactory requestFactory,
							final WebClient webClient,
							final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
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
		restTemplate = restTemplateBuilder
				.rootUri(safSelvbetjeningProperties.getEndpoints().getFagarkiv())
				.basicAuthentication(
						safSelvbetjeningProperties.getServiceuser().getUsername(),
						safSelvbetjeningProperties.getServiceuser().getPassword()
				)
				.requestFactory(() -> requestFactory)
				.build();
	}

	@CircuitBreaker(name = DOKARKIV_DOKUMENTOVERSIKT)
	public FinnJournalposterResponseTo finnJournalposter(final FinnJournalposterRequestTo request) {
		try {
			HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(request, baseHttpHeaders());
			return restTemplate.exchange("/finnjournalposter",
					POST,
					requestEntity,
					FinnJournalposterResponseTo.class).getBody();
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException("Teknisk feil ved å finne journalpost for " + request, e);
		}
	}

	@CircuitBreaker(name = DOKARKIV_METADATA)
	public ArkivJournalpost journalpost(String journalpostId, String dokumentInfoId, Set<String> fields) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}", "dokumentInfoId", "{dokumentInfoId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder
							.build(journalpostId, dokumentInfoId);
				})
				.attributes(getOAuth2AuthorizedClient())
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalpost.class)
				.doOnError(handleErrorJournalpost(journalpostId, dokumentInfoId))
				.block();
	}

	private Consumer<Throwable> handleErrorJournalpost(String journalpostId, String dokumentInfoId) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				throw new JournalpostIkkeFunnetException(format("Journalpost med journalpostId=%s, dokumentInfoId=%s ikke funnet i Joark.",
						journalpostId, dokumentInfoId), notFound);
			}
			if (error instanceof WebClientResponseException webException) {
				if (webException.getStatusCode().is4xxClientError()) {
					throw new ConsumerFunctionalException(format("hentJournalpost feilet funksjonelt. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
							webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()));
				} else {
					throw new ConsumerTechnicalException(String.format("hentJournalpost feilet teknisk. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
							webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()), webException);
				}
			}
		};
	}

	@CircuitBreaker(name = DOKARKIV_HENTDOKUMENT)
	public HentDokumentResponseTo hentDokument(final String dokumentInfoId, final String variantFormat) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/hentdokument/{dokumentInfoId}/{variantFormat}")
						.build(dokumentInfoId, variantFormat))
				.attributes(getOAuth2AuthorizedClient())
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
				.doOnError(handleErrorHentDokument(dokumentInfoId, variantFormat))
				.block();
	}

	private Consumer<Throwable> handleErrorHentDokument(String dokumentInfoId, String variantFormat) {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				if (NOT_FOUND.equals(((WebClientResponseException) error).getStatusCode())) {
					throw new DokumentIkkeFunnetException("Fant ikke dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, error);
				}
				throw new ConsumerFunctionalException("Funksjonell feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, error);
			} else {
				throw new ConsumerTechnicalException("Teknisk feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, error);
			}
		};
	}

	private HttpHeaders baseHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(NAV_CALLID, getCallId());
		return headers;
	}

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(getOAuth2AuthorizeRequestForAzure(CLIENT_REGISTRATION_DOKARKIV));
		return oauth2AuthorizedClient(clientMono.block());
	}
}
