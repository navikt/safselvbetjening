package no.nav.safselvbetjening.consumer.fagarkiv;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.springframework.http.HttpHeaders.ACCEPT_ENCODING;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Component
public class FagarkivConsumer {
	private static final String FAGARKIV_INSTANCE = "fagarkiv";
	private static final String FAGARKIVTILGANGJOURNALPOST_INSTANCE = "fagarkivtilgangjournalpost";
	private static final String FAGARKIVHENTDOKUMENT_INSTANCE = "fagarkivhentdokument";
	private final RestTemplate restTemplate;

	public FagarkivConsumer(final RestTemplateBuilder restTemplateBuilder,
							final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final ClientHttpRequestFactory requestFactoryFagarkiv) {
		restTemplate = restTemplateBuilder
				.rootUri(safSelvbetjeningProperties.getEndpoints().getFagarkiv())
				.basicAuthentication(
						safSelvbetjeningProperties.getServiceuser().getUsername(),
						safSelvbetjeningProperties.getServiceuser().getPassword()
				)
				.requestFactory(() -> requestFactoryFagarkiv)
				.build();
	}

	@CircuitBreaker(name = FAGARKIV_INSTANCE)
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

	@CircuitBreaker(name = FAGARKIVTILGANGJOURNALPOST_INSTANCE)
	public TilgangJournalpostResponseTo tilgangJournalpost(final String journalpostId, final String dokumentInfoId, final String variantFormat) {
		try {
			return restTemplate.exchange("/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}",
					GET,
					new HttpEntity<>(httpHeadersWithEncoding()),
					TilgangJournalpostResponseTo.class,
					journalpostId, dokumentInfoId, variantFormat).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			throw new JournalpostIkkeFunnetException("Fant ikke journalpost for tilgangskontroll med journalpostId=" +
					journalpostId + ", dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, e);
		} catch (HttpClientErrorException e) {
			throw new ConsumerFunctionalException("Funksjonell feil mot tilgangJournalpost for journalpost med journalpostId=" +
					journalpostId + "dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, e);
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException("Teknisk feil mot tilgangJournalpost for journalpost med journalpostId=" +
					journalpostId + "dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, e);
		}
	}

	@CircuitBreaker(name = FAGARKIVHENTDOKUMENT_INSTANCE)
	public HentDokumentResponseTo hentDokument(final String dokumentInfoId, final String variantFormat) {
		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange("/hentdokument/{dokumentInfoId}/{variantFormat}",
					GET,
					new HttpEntity<>(baseHttpHeaders()), String.class, dokumentInfoId, variantFormat);
			return HentDokumentResponseTo.builder()
					.dokument(responseEntity.getBody())
					.mediaType(responseEntity.getHeaders().getContentType())
					.build();
		} catch (HttpClientErrorException.NotFound e) {
			throw new DokumentIkkeFunnetException("Fant ikke dokument med dokumentInfoId=" + dokumentInfoId +
					", variantFormat=" + variantFormat, e);
		} catch (HttpClientErrorException e) {
			throw new ConsumerFunctionalException("Funksjonell feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, e);
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException("Teknisk feil mot hentDokument for dokument med dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat, e);
		}
	}

	private HttpHeaders baseHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(NAV_CALLID, getCallId());
		return headers;
	}

	private HttpHeaders httpHeadersWithEncoding() {
		HttpHeaders headers = baseHttpHeaders();
		headers.set(ACCEPT_ENCODING, "gzip");
		return headers;
	}
}
