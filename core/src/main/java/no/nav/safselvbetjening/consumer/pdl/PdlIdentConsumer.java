package no.nav.safselvbetjening.consumer.pdl;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.CallIdExchangeFilterFunction;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;

import static no.nav.safselvbetjening.azure.AzureProperties.CLIENT_REGISTRATION_PDL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

/**
 * PDL implementasjon av {@link IdentConsumer}
 */
@Component
class PdlIdentConsumer implements IdentConsumer {

	static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";
	private static final String PDL_INSTANCE = "pdl";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final WebClient webClient;
	private final CircuitBreaker circuitBreaker;
	private final Retry retry;

	public PdlIdentConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final WebClient webClient,
							final CircuitBreakerRegistry circuitBreakerRegistry,
							final RetryRegistry retryRegistry) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.webClient = webClient.mutate()
				.filter(new CallIdExchangeFilterFunction(HEADER_NAV_CALL_ID))
				.defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(PDL_INSTANCE);
		this.retry = retryRegistry.retry(PDL_INSTANCE);
	}

	@Override
	public List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException {
		PdlResponse pdlResponse = webClient.post()
				.uri(safSelvbetjeningProperties.getEndpoints().getPdl().getUrl())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.bodyValue(mapHentIdenterQuery(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.onErrorMap(this::mapPdlError)
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.transformDeferred(RetryOperator.of(retry))
				.block();

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

	private Throwable mapPdlError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", error);
		} else {
			return new ConsumerTechnicalException("Kall mot pdl feilet teknisk", error);
		}
	}
}
