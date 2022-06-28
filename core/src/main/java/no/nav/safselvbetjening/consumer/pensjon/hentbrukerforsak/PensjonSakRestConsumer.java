package no.nav.safselvbetjening.consumer.pensjon.hentbrukerforsak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.NavHeaders;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.azure.TokenConsumer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_SAK_REST_INSTANCE = "pensjonSakRest";
	private final RestTemplate restTemplate;
	private final String pensjonsakUrl;
	private final TokenConsumer tokenConsumer;
	private final String pensjonsakScope;

	public PensjonSakRestConsumer(final RestTemplateBuilder restTemplateBuilder,
								  final TokenConsumer tokenConsumer,
								  final ClientHttpRequestFactory clientHttpRequestFactory,
								  final SafSelvbetjeningProperties safSelvbetjeningProperties) {
		SafSelvbetjeningProperties.AzureEndpoint pensjonsak = safSelvbetjeningProperties.getEndpoints().getPensjonsak();
		this.pensjonsakScope = pensjonsak.getScope();
		this.pensjonsakUrl = pensjonsak.getUrl();
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(60))
				.setConnectTimeout(Duration.ofSeconds(5))
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.tokenConsumer = tokenConsumer;
	}

	@Retry(name = PENSJON_SAK_REST_INSTANCE)
	@CircuitBreaker(name = PENSJON_SAK_REST_INSTANCE)
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		try {
			HttpHeaders headers = createHeaders();
			headers.add("sakId", sakId);
			headers.add(NavHeaders.NAV_CALLID, getCallId());

			HentBrukerForSakResponseTo hentBrukerForSakResponseTo = restTemplate.exchange(pensjonsakUrl, GET, new HttpEntity<HentBrukerForSakResponseTo>(headers), HentBrukerForSakResponseTo.class)
					.getBody();
			if (hentBrukerForSakResponseTo == null || hentBrukerForSakResponseTo.getFnr() == null || hentBrukerForSakResponseTo.getFnr().isEmpty()) {
				throw new PensjonsakIkkeFunnetException("hentBrukerForSak returnerte tomt fødselsnummer for sakId={}. Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken" + sakId);
			} else {
				return hentBrukerForSakResponseTo;
			}
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException("hentBrukerForSak feilet teknisk med statusKode={}. Feilmelding={}" + e
					.getStatusCode() + e.getMessage(), e);
		} catch (HttpClientErrorException e) {
			throw new ConsumerFunctionalException("hentBrukerForSak feilet funksjonelt med statusKode={}. Feilmelding={}" + e
					.getStatusCode() + e.getMessage(), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(tokenConsumer.getClientCredentialToken(pensjonsakScope).getAccess_token());
		headers.set(NAV_CALLID, getCallId());
		return headers;
	}
}
