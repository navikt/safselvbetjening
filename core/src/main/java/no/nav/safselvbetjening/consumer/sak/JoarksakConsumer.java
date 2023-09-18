package no.nav.safselvbetjening.consumer.sak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static org.springframework.http.HttpMethod.GET;

/**
 * Henter arkivsaker fra sak applikasjonen.
 * Master for data er SAK tabellen i joark databasen.
 */
@Slf4j
@Component
public class JoarksakConsumer {

	private static final String ARKIVSAK_INSTANCE = "arkivsak";
	private static final String HEADER_SAK_CORRELATION_ID = "X-Correlation-ID";

	private final RestTemplate restTemplate;
	private final String sakUrl;

	public JoarksakConsumer(final RestTemplateBuilder restTemplateBuilder,
							final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final ClientHttpRequestFactory requestFactory) {
		this.sakUrl = safSelvbetjeningProperties.getEndpoints().getSak();
		this.restTemplate = restTemplateBuilder
				.basicAuthentication(
						safSelvbetjeningProperties.getServiceuser().getUsername(),
						safSelvbetjeningProperties.getServiceuser().getPassword()
				)
				.requestFactory(() -> requestFactory)
				.build();
	}

	@Retry(name = ARKIVSAK_INSTANCE)
	@CircuitBreaker(name = ARKIVSAK_INSTANCE)
	public List<Joarksak> hentSaker(final List<String> aktoerId, final List<String> tema) {
		if(tema.isEmpty()) {
			return new ArrayList<>();
		}
		final UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(sakUrl)
				.queryParam("aktoerId", aktoerId)
				.queryParam("tema", tema);
		return hentSaker(uri.toUriString());
	}

	private List<Joarksak> hentSaker(final String uri) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set(HEADER_SAK_CORRELATION_ID, getCallId());
			ResponseEntity<List<Joarksak>> response = restTemplate.exchange(uri, GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
			});
			return response.getBody();
		} catch (HttpServerErrorException e) {
			throw new ConsumerTechnicalException("Teknisk feil. Kunne ikke hente saker for bruker fra sak.", e);
		} catch (HttpClientErrorException e) {
			throw new ConsumerFunctionalException("Funksjonell feil. Kunne ikke hente saker for bruker fra sak.", e);
		}
	}
}
