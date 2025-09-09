package no.nav.safselvbetjening.consumer.sak;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.consumer.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Henter arkivsaker fra sak applikasjonen.
 * Master for data er SAK tabellen i joark databasen.
 */
@Component
public class SakConsumer {

	private static final String ARKIVSAK_INSTANCE = "arkivsak";
	private static final String HEADER_SAK_CORRELATION_ID = "X-Correlation-ID";

	private final RestClient restClientTexas;
	private final ObjectMapper objectMapper;
	private final String sakScope;

	public SakConsumer(SafSelvbetjeningProperties safSelvbetjeningProperties,
					   RestClient restClientTexas,
					   ObjectMapper objectMapper) {
		this.restClientTexas = restClientTexas.mutate()
				.baseUrl(safSelvbetjeningProperties.getEndpoints().getSak().getUrl())
				.build();
		this.sakScope = safSelvbetjeningProperties.getEndpoints().getSak().getScope();
		this.objectMapper = objectMapper;
	}

	@CircuitBreaker(name = ARKIVSAK_INSTANCE)
	@Retry(name = ARKIVSAK_INSTANCE)
	public List<Joarksak> hentSaker(final List<String> aktoerId, final List<String> tema) {
		if (tema.isEmpty()) {
			return new ArrayList<>();
		}

		return restClientTexas.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("aktoerId", aktoerId)
						.queryParam("tema", tema)
						.build())
				.header(HEADER_SAK_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.accept(APPLICATION_JSON)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (request, response) -> {
					String feilmelding = "Henting av saker for bruker feilet %s med statuskode=%s og feilmelding=%s.";
					ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);

					if (response.getStatusCode().is4xxClientError()) {
						throw new ConsumerFunctionalException(feilmelding.formatted("funksjonelt", response.getStatusCode(), problemDetail));
					} else {
						throw new ConsumerTechnicalException(feilmelding.formatted("teknisk", response.getStatusCode(), problemDetail));
					}
				})
				.body(new ParameterizedTypeReference<>() {
				});
	}

}