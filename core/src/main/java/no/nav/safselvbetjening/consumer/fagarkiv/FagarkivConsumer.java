package no.nav.safselvbetjening.consumer.fagarkiv;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import no.nav.safselvbetjening.NavHeaders;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class FagarkivConsumer {
	private static final String FAGARKIV_INSTANCE = "fagarkiv";
	private final RestTemplate restTemplate;

	@Autowired
	public FagarkivConsumer(final RestTemplateBuilder restTemplateBuilder,
							final SafSelvbetjeningProperties safSelvbetjeningProperties) {
		restTemplate = restTemplateBuilder
				.rootUri(safSelvbetjeningProperties.getEndpoints().getFagarkiv())
				.basicAuthentication(safSelvbetjeningProperties.getServiceuser().getUsername(),
						safSelvbetjeningProperties.getServiceuser().getPassword())
				.setReadTimeout(Duration.ofSeconds(60))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
	}

	@CircuitBreaker(name = FAGARKIV_INSTANCE)
	public FinnJournalposterResponseTo finnJournalposter(final FinnJournalposterRequestTo request) {
		try {
			HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(request, createCorrelationIdHeader());
			return restTemplate.exchange("/finnjournalposter",
					HttpMethod.POST,
					requestEntity,
					FinnJournalposterResponseTo.class).getBody();
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException("Teknisk feil ved å finne journalpost for " + request, e);
		}
	}

	@CircuitBreaker(name = FAGARKIV_INSTANCE)
	public TilgangJournalpostResponseTo tilgangJournalpost(final String journalpostId, final String dokumentInfoId, final String variantFormat) {
		return restTemplate.exchange("/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}",
				HttpMethod.GET,
				new HttpEntity<>(createCorrelationIdHeader()),
				TilgangJournalpostResponseTo.class,
				journalpostId, dokumentInfoId, variantFormat).getBody();
	}

	public HentDokumentResponseTo hentDokument(final String dokumentInfoId, final String variantFormat) {
		ResponseEntity<String> responseEntity = restTemplate.exchange("/hentdokument/{dokumentInfoId}/{variantFormat}",
				HttpMethod.GET,
				new HttpEntity<>(createCorrelationIdHeader()), String.class, dokumentInfoId, variantFormat);
		return HentDokumentResponseTo.builder()
				.dokument(responseEntity.getBody())
				.mediaType(responseEntity.getHeaders().getContentType())
				.build();
	}

	private HttpHeaders createCorrelationIdHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(NavHeaders.NAV_CALLID, UUID.randomUUID().toString());
		return headers;
	}
}
