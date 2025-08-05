package no.nav.safselvbetjening.consumer.sak;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.CallIdExchangeFilterFunction;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.azure.AzureProperties.CLIENT_REGISTRATION_SAK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

/**
 * Henter arkivsaker fra sak applikasjonen.
 * Master for data er SAK tabellen i joark databasen.
 */
@Component
public class JoarksakConsumer {

	private static final String ARKIVSAK_INSTANCE = "arkivsak";
	private static final String HEADER_SAK_CORRELATION_ID = "X-Correlation-ID";

	private final WebClient webClient;
	private final CircuitBreaker circuitBreaker;
	private final Retry retry;

	public JoarksakConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final WebClient webClient,
							final CircuitBreakerRegistry circuitBreakerRegistry,
							final RetryRegistry retryRegistry) {
		this.webClient = webClient.mutate()
				.baseUrl(safSelvbetjeningProperties.getEndpoints().getSak().getUrl())
				.filter(new CallIdExchangeFilterFunction(HEADER_SAK_CORRELATION_ID))
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(ARKIVSAK_INSTANCE);
		this.retry = retryRegistry.retry(ARKIVSAK_INSTANCE);
	}

	public List<Joarksak> hentSaker(final List<String> aktoerId, final List<String> tema) {
		if (tema.isEmpty()) {
			return new ArrayList<>();
		}

		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("aktoerId", aktoerId)
						.queryParam("tema", tema)
						.build())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_SAK))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Joarksak>>() {
				})
				.onErrorMap(this::mapSakerError)
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.transformDeferred(RetryOperator.of(retry))
				.block();
	}

	private Throwable mapSakerError(Throwable error) {
		if (error instanceof WebClientResponseException webException && webException.getStatusCode().is4xxClientError()) {
			return new ConsumerFunctionalException("Funksjonell feil. Kunne ikke hente saker for bruker fra sak.", error);
		}
		return new ConsumerTechnicalException("Teknisk feil. Kunne ikke hente saker for bruker fra sak.", error);
	}

}