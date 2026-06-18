package no.nav.safselvbetjening.consumer.pdl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.safselvbetjening.consumer.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * PDL implementasjon av {@link IdentConsumer}
 */
@Slf4j
@Component
class PdlIdentConsumer implements IdentConsumer {

	private static final String PDL_INSTANCE = "pdl";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestClient restClient;
	private final String targetScope;

	public PdlIdentConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final RestClient restClientTexas) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(safSelvbetjeningProperties.getEndpoints().getPdl().getUrl())
				.defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = safSelvbetjeningProperties.getEndpoints().getPdl().getScope();
	}

	@Override
	@CircuitBreaker(name = PDL_INSTANCE)
	@Retryable(includes = ConsumerTechnicalException.class)
	public List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException {
		PdlResponse pdlResponse = restClient.post()
				.attribute(TARGET_SCOPE, targetScope)
				.body(mapHentIdenterQuery(ident))
				.retrieve()
				.body(PdlResponse.class);

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentIdenter().getIdenter();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().getFirst().getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke aktørid for person i pdl.");
			}
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
		}
	}

	private PdlRequest mapHentIdenterQuery(final String ident) {
		String query = "query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, historikk: true) {identer { ident gruppe historisk } } }";
		final HashMap<String, String> variables = new HashMap<>();
		variables.put("ident", ident);

		return new PdlRequest(query, variables);
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), UTF_8);
		if (response.getStatusCode().is4xxClientError()) {
			throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt med status=%s, body=%s"
					.formatted(response.getStatusCode(), body));
		}
		throw new ConsumerTechnicalException("Kall mot pdl feilet teknisk med status=%s, body=%s"
				.formatted(response.getStatusCode(), body));
	}
}
