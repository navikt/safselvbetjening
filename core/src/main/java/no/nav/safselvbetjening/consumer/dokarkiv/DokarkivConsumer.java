package no.nav.safselvbetjening.consumer.dokarkiv;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalposter;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.FinnJournalposterRequest;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static java.lang.String.format;
import static no.nav.safselvbetjening.consumer.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@Slf4j
@Component
public class DokarkivConsumer {

	private static final String DOKARKIV_METADATA = "dokarkivmetadata";
	private static final String DOKARKIV_DOKUMENTOVERSIKT = "dokarkivdokumentoversikt";
	private static final String DOKARKIV_HENTDOKUMENT = "dokarkivhentdokument";
	private static final String SERVICE_NAME = "dokarkiv";

	private final RestClient restClient;
	private final String targetScope;

	public DokarkivConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final RestClient restClientTexas) {
		SafSelvbetjeningProperties.AzureEndpoint dokarkiv = safSelvbetjeningProperties.getEndpoints().getDokarkiv();
		this.restClient = restClientTexas.mutate()
				.baseUrl(dokarkiv.getUrl())
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = dokarkiv.getScope();
	}

	@CircuitBreaker(name = DOKARKIV_DOKUMENTOVERSIKT)
	@Retryable(includes = {ConsumerTechnicalException.class, ResourceAccessException.class})
	public ArkivJournalposter finnJournalposter(FinnJournalposterRequest request, Set<String> fields) {
		return restClient.post()
				.uri(uriBuilder -> {
					uriBuilder.path("/finnjournalposter");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build();
				})
				.body(request)
				.attribute(TARGET_SCOPE, targetScope)
				.accept(APPLICATION_JSON)
				.retrieve()
				.body(ArkivJournalposter.class);
	}

	@CircuitBreaker(name = DOKARKIV_METADATA)
	@Retryable(includes = {ConsumerTechnicalException.class, ResourceAccessException.class})
	public ArkivJournalpost journalpost(String journalpostId, String dokumentInfoId, Set<String> fields) {
		return restClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}", "dokumentInfoId", "{dokumentInfoId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(journalpostId, dokumentInfoId);
				})
				.attribute(TARGET_SCOPE, targetScope)
				.accept(APPLICATION_JSON)
				.exchange((_, res) -> {
					if (res.getStatusCode().is2xxSuccessful()) {
						return res.bodyTo(ArkivJournalpost.class);
					} else if (NOT_FOUND.isSameCodeAs(res.getStatusCode())) {
						throw new JournalpostIkkeFunnetException(
								format("Journalpost med journalpostId=%s, dokumentInfoId=%s ikke funnet i Joark", journalpostId, dokumentInfoId));
					} else if (res.getStatusCode().is4xxClientError()) {
						String body = res.bodyTo(String.class);
						throw new ConsumerFunctionalException(
								format("hentJournalpost feilet funksjonelt. status=%s, journalpostId=%s, dokumentInfoId=%s, body=%s",
										res.getStatusCode(), journalpostId, dokumentInfoId, body));
					} else {
						String body = res.bodyTo(String.class);
						throw new ConsumerTechnicalException(
								format("hentJournalpost feilet teknisk. status=%s, journalpostId=%s, dokumentInfoId=%s, body=%s",
										res.getStatusCode(), journalpostId, dokumentInfoId, body));
					}
				});
	}

	@CircuitBreaker(name = DOKARKIV_METADATA)
	@Retryable(includes = {ConsumerTechnicalException.class, ResourceAccessException.class})
	public ArkivJournalpost journalpost(long journalpostId, Set<String> fields) {
		return restClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(journalpostId);
				})
				.attribute(TARGET_SCOPE, targetScope)
				.accept(APPLICATION_JSON)
				.retrieve()
				.body(ArkivJournalpost.class);
	}

	@CircuitBreaker(name = DOKARKIV_HENTDOKUMENT)
	@Retryable(includes = {ConsumerTechnicalException.class, ResourceAccessException.class})
	public HentDokumentResponseTo hentDokument(final String dokumentInfoId, final TilgangVariantFormat variantFormat) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/hentdokument/{dokumentInfoId}/{variantFormat}")
						.build(dokumentInfoId, variantFormat.name()))
				.attribute(TARGET_SCOPE, targetScope)
				.accept(APPLICATION_PDF)
				.exchange((_, res) -> {
					if (res.getStatusCode().is2xxSuccessful()) {
						return HentDokumentResponseTo.builder()
								.dokument(res.bodyTo(byte[].class))
								.mediaType(res.getHeaders().getContentType())
								.build();
					} else if (NOT_FOUND.isSameCodeAs(res.getStatusCode())) {
						throw new DokumentIkkeFunnetException("Fant ikke dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat);
					} else if (res.getStatusCode().is4xxClientError()) {
						throw new ConsumerFunctionalException("Funksjonell feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat);
					} else {
						throw new ConsumerTechnicalException("Teknisk feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat);
					}
				});
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		String feilmelding = "Kall mot %s feilet %s med status=%s, body=%s"
				.formatted(SERVICE_NAME,
						response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
						response.getStatusCode(), body);
		if (response.getStatusCode().is4xxClientError()) {
			if (NOT_FOUND.isSameCodeAs(response.getStatusCode())) {
				throw new JournalpostIkkeFunnetException(format("Journalpost ikke funnet i Joark. status=%s", response.getStatusCode()));
			}
			throw new ConsumerFunctionalException(feilmelding);
		}
		throw new ConsumerTechnicalException(feilmelding);
	}
}
