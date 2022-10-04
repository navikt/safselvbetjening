package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.azure.TokenConsumer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import java.time.Duration;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;
import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_SAK_REST_INSTANCE = "pensjonSakRest";
	private final RestTemplate restTemplate;
	private final TokenConsumer tokenConsumer;
	private final String pensjonsakScope;

	public PensjonSakRestConsumer(final RestTemplateBuilder restTemplateBuilder,
								  final TokenConsumer tokenConsumer,
								  final ClientHttpRequestFactory clientHttpRequestFactory,
								  final SafSelvbetjeningProperties safSelvbetjeningProperties) {
		SafSelvbetjeningProperties.AzureEndpoint pensjonsak = safSelvbetjeningProperties.getEndpoints().getPensjon();
		this.pensjonsakScope = pensjonsak.getScope();
		this.restTemplate = restTemplateBuilder
				.rootUri(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl())
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

			HentBrukerForSakResponseTo hentBrukerForSakResponseTo = restTemplate.exchange("/api/pip/hentBrukerOgEnhetstilgangerForSak/v1", GET, new HttpEntity<HentBrukerForSakResponseTo>(headers), HentBrukerForSakResponseTo.class)
					.getBody();
			if (hentBrukerForSakResponseTo == null || hentBrukerForSakResponseTo.fnr() == null || hentBrukerForSakResponseTo.fnr().isEmpty()) {
				throw new PensjonsakIkkeFunnetException("hentBrukerForSak returnerte tomt fødselsnummer for sakId={}. Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken" + sakId);
			} else {
				return hentBrukerForSakResponseTo;
			}
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException(String.format("hentBrukerForSak feilet teknisk med statusKode=%s. Feilmelding=%s", e.getStatusCode(),e.getMessage()), e);
		} catch (HttpClientErrorException e) {
			throw new ConsumerFunctionalException(String.format("hentBrukerForSak feilet funksjonelt med statusKode=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	@Retry(name = PENSJON_SAK_REST_INSTANCE)
	@CircuitBreaker(name = PENSJON_SAK_REST_INSTANCE)
	public List<Pensjonsak> hentPensjonssaker(final String personident) {
		if (isBlank(personident)) {
			return emptyList();
		}

		try {
			HttpHeaders headers = createHeaders();
			headers.add("fnr", personident);

			return requireNonNullElse(
					restTemplate.exchange("/springapi/sak/sammendrag", GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<Pensjonsak>>() {})
							.getBody(),
					emptyList()
			);
		} catch (UnknownContentTypeException e) {
			throw new ConsumerTechnicalException(String.format("hentPensjonssaker feilet teknisk. Feilmelding=%s", e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException(String.format("hentPensjonssaker feilet teknisk med statusKode=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == NOT_FOUND) {
				throw new ConsumerFunctionalException(
						String.format("hentPensjonssaker feilet funksjonelt (person ikke funnet). StatusKode=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e
				);
			}
			throw new ConsumerFunctionalException(
					String.format("hentPensjonssaker feilet funksjonelt med statusKode=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e
			);
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
