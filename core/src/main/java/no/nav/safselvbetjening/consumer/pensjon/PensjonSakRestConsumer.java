package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static no.nav.safselvbetjening.consumer.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_INSTANCE = "pensjon";

	private final RestClient restClient;
	private final String targetScope;

	public PensjonSakRestConsumer(
			final SafSelvbetjeningProperties safSelvbetjeningProperties,
			final RestClient restClientTexas) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = safSelvbetjeningProperties.getEndpoints().getPensjon().getScope();
	}

	@CircuitBreaker(name = PENSJON_INSTANCE)
	@Retryable(includes = {ConsumerTechnicalException.class, ResourceAccessException.class})
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		var result = restClient.get()
				.uri("/pip/hentBrukerOgEnhetstilgangerForSak/v1")
				.header("sakId", sakId)
				.attribute(TARGET_SCOPE, targetScope)
				.retrieve()
				.body(HentBrukerForSakResponseTo.class);

		if (result == null || result.fnr() == null || result.fnr().isEmpty()) {
			throw new PensjonsakIkkeFunnetException("hentBrukerForSak returnerte tomt fødselsnummer for sakId=" + sakId + ". " +
					"Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken i pesys");
		} else {
			return result;
		}
	}

	@CircuitBreaker(name = PENSJON_INSTANCE)
	@Retryable(includes = {ConsumerTechnicalException.class, ResourceAccessException.class})
	public List<Pensjonsak> hentPensjonssaker(final String personident) {
		if (isBlank(personident)) {
			return emptyList();
		}

		return restClient.get()
				.uri("/sak/sammendrag")
				.header("fnr", personident)
				.attribute(TARGET_SCOPE, targetScope)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), UTF_8);
		if (response.getStatusCode().is4xxClientError()) {
			throw new ConsumerFunctionalException("Kall mot pensjon feilet funksjonelt med status=%s, body=%s"
					.formatted(response.getStatusCode(), body));
		}
		throw new ConsumerTechnicalException("Kall mot pensjon feilet teknisk med status=%s, body=%s"
				.formatted(response.getStatusCode(), body));
	}
}
