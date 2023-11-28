package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.CallIdExchangeFilterFunction;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static no.nav.safselvbetjening.azure.AzureProperties.CLIENT_REGISTRATION_PENSJON;
import static no.nav.safselvbetjening.consumer.ConsumerExceptionHandlers.handleMidlertidigNginxError;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_INSTANCE = "pensjon";

	private final WebClient webClient;
	private final CircuitBreaker circuitBreaker;
	private final Retry retry;

	public PensjonSakRestConsumer(
			final SafSelvbetjeningProperties safSelvbetjeningProperties,
			final WebClient webClient,
			final CircuitBreakerRegistry circuitBreakerRegistry,
			final RetryRegistry retryRegistry) {
		this.webClient = webClient.mutate()
				.baseUrl(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new CallIdExchangeFilterFunction(NAV_CALLID))
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(PENSJON_INSTANCE);
		this.retry = retryRegistry.retry(PENSJON_INSTANCE);
	}

	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		var result = webClient.get()
				.uri("/pen/api/pip/hentBrukerOgEnhetstilgangerForSak/v1")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PENSJON))
				.header("sakId", sakId)
				.retrieve()
				.bodyToMono(HentBrukerForSakResponseTo.class)
				.doOnError(handleErrorBrukerForSak())
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.transformDeferred(RetryOperator.of(retry))
				.block();

		if (result == null || result.fnr() == null || result.fnr().isEmpty()) {
			throw new PensjonsakIkkeFunnetException("hentBrukerForSak returnerte tomt fødselsnummer for sakId=" + sakId + ". " +
													"Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken i pesys");
		} else {
			return result;
		}
	}

	private Consumer<Throwable> handleErrorBrukerForSak() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				if (error instanceof WebClientResponseException.NotFound notFound) {
					handleMidlertidigNginxError(notFound);
				}
				throw new ConsumerFunctionalException(format("hentBrukerForSak feilet funksjonelt med statuskode=%s. Feilmelding=%s", response.getStatusCode(), response.getMessage()), error);
			} else {
				throw new ConsumerTechnicalException(format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
			}
		};
	}

	public List<Pensjonsak> hentPensjonssaker(final String personident) {
		if (isBlank(personident)) {
			return emptyList();
		}

		return webClient.get()
				.uri("/pen/springapi/sak/sammendrag")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PENSJON))
				.header("fnr", personident)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Pensjonsak>>() {
				})
				.doOnError(handleErrorPensjonssaker())
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.transformDeferred(RetryOperator.of(retry))
				.block();
	}

	private Consumer<Throwable> handleErrorPensjonssaker() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				if (error instanceof WebClientResponseException.NotFound notFound) {
					handleMidlertidigNginxError(notFound);
					throw new ConsumerFunctionalException(
							format("hentPensjonssaker feilet funksjonelt (person ikke funnet). Statuskode=%s. Feilmelding=%s", response.getStatusCode(), error.getMessage()), error);
				}
				throw new ConsumerFunctionalException(
						format("hentPensjonssaker feilet funksjonelt med statuskode=%s. Feilmelding=%s", response.getStatusCode(), error.getMessage()), error);
			} else {
				throw new ConsumerTechnicalException(format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
			}
		};
	}

}
