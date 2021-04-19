package no.nav.safselvbetjening.consumer.pensjon.hentbrukerforsak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.NavHeaders;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.beans.factory.annotation.Value;
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
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_SAK_REST_INSTANCE = "pensjonSakRest";
	private final RestTemplate restTemplate;
	private final String pensjonsakUrl;

	public PensjonSakRestConsumer(final RestTemplateBuilder restTemplateBuilder,
								  final SafSelvbetjeningProperties safSelvbetjeningProperties,
								  final ClientHttpRequestFactory clientHttpRequestFactory,
								  @Value("${safselvbetjening.endpoints.pensjonsak}") String pensjonsakUrl) {
		this.restTemplate = restTemplateBuilder
				.basicAuthentication(safSelvbetjeningProperties.getServiceuser().getUsername(),
						safSelvbetjeningProperties.getServiceuser().getPassword())
				.setReadTimeout(Duration.ofSeconds(60))
				.setConnectTimeout(Duration.ofSeconds(5))
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.pensjonsakUrl = pensjonsakUrl;
	}

	@Retry(name = PENSJON_SAK_REST_INSTANCE)
	@CircuitBreaker(name = PENSJON_SAK_REST_INSTANCE)
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		try {
			HttpHeaders headers = new HttpHeaders();
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
}
