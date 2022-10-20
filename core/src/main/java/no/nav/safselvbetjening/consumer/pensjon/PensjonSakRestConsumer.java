package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.azure.AzureProperties;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_INSTANCE = "pensjon";

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final WebClient webClient;
	private final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

	public PensjonSakRestConsumer(
			final SafSelvbetjeningProperties safSelvbetjeningProperties,
			final WebClient webClient,
			final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager
	) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.webClient = webClient;
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
	}

	@Retry(name = PENSJON_INSTANCE)
	@CircuitBreaker(name = PENSJON_INSTANCE)
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {

		var result = webClient.get()
				.uri(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl() + "/api/pip/hentBrukerOgEnhetstilgangerForSak/v1")
				.attributes(getOAuth2AuthorizedClient())
				.headers(this::createHeaders)
				.header("sakId", sakId)
				.retrieve()
				.bodyToMono(HentBrukerForSakResponseTo.class)
				.doOnError(handleErrorBrukerForSak())
				.block();

		if (result == null || result.fnr() == null || result.fnr().isEmpty()) {
			throw new PensjonsakIkkeFunnetException("hentBrukerForSak returnerte tomt fødselsnummer for sakId={}. Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken" + sakId);
		} else {
			return result;
		}
	}

	private Consumer<Throwable> handleErrorBrukerForSak() {
		return error -> {
			if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
				throw new ConsumerFunctionalException(String.format("hentBrukerForSak feilet funksjonelt med statuskode=%s. Feilmelding=%s", response.getStatusCode(), error.getMessage()), error);
			} else {
				throw new ConsumerTechnicalException(String.format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
			}
		};
	}

	@Retry(name = PENSJON_INSTANCE)
	@CircuitBreaker(name = PENSJON_INSTANCE)
	public List<Pensjonsak> hentPensjonssaker(final String personident) {
		if (isBlank(personident)) {
			return emptyList();
		}

		return webClient.get()
				.uri(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl() + "/springapi/sak/sammendrag")
				.attributes(getOAuth2AuthorizedClient())
				.headers(this::createHeaders)
				.header("fnr", personident)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Pensjonsak>>() {})
				.doOnError(handleErrorPensjonssaker())
				.block();
	}

	private Consumer<Throwable> handleErrorPensjonssaker() {
		return error -> {
			if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
				if (response.getStatusCode() == NOT_FOUND) {
					throw new ConsumerFunctionalException(
							String.format("hentPensjonssaker feilet funksjonelt (person ikke funnet). Statuskode=%s. Feilmelding=%s", response.getStatusCode(), error.getMessage()), error
					);
				}
				throw new ConsumerFunctionalException(
						String.format("hentPensjonssaker feilet funksjonelt med statuskode=%s. Feilmelding=%s", response.getStatusCode(), error.getMessage()), error
				);
			} else {
				throw new ConsumerTechnicalException(String.format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
			}
		};
	}

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(AzureProperties.getOAuth2AuthorizeRequestForAzurePensjon());
		return ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(clientMono.block());
	}

	private void createHeaders(HttpHeaders headers) {
		headers.setContentType(APPLICATION_JSON);
		headers.set(NAV_CALLID, getCallId());
	}
}
