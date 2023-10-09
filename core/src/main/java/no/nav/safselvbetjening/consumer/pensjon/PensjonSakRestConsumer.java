package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.CallIdExchangeFilterFunction;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static no.nav.safselvbetjening.azure.AzureProperties.CLIENT_REGISTRATION_PENSJON;
import static no.nav.safselvbetjening.azure.AzureProperties.getOAuth2AuthorizeRequestForAzure;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_INSTANCE_BRUKER_FOR_SAK = "pensjonbrukerforsak";
	private static final String PENSJON_INSTANCE_PENSJONSSAKER = "pensjonpensjonssaker";

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final WebClient webClient;
	private final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

	public PensjonSakRestConsumer(
			final SafSelvbetjeningProperties safSelvbetjeningProperties,
			final WebClient webClient,
			ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
		this.webClient = webClient.mutate()
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new CallIdExchangeFilterFunction(NAV_CALLID))
				.build();

	}

	@Retry(name = PENSJON_INSTANCE_BRUKER_FOR_SAK)
	@CircuitBreaker(name = PENSJON_INSTANCE_BRUKER_FOR_SAK)
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		var result = webClient.get()
				.uri(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl() + "/pen/api/pip/hentBrukerOgEnhetstilgangerForSak/v1")
				.attributes(getOAuth2AuthorizedClient())
				.header("sakId", sakId)
				.retrieve()
				.bodyToMono(HentBrukerForSakResponseTo.class)
				.doOnError(handleErrorBrukerForSak())
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
				throw new ConsumerFunctionalException(format("hentBrukerForSak feilet funksjonelt med statuskode=%s. Feilmelding=%s", response.getStatusCode(), response.getMessage()), error);
			} else {
				throw new ConsumerTechnicalException(format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
			}
		};
	}

	@Retry(name = PENSJON_INSTANCE_PENSJONSSAKER)
	@CircuitBreaker(name = PENSJON_INSTANCE_PENSJONSSAKER)
	public List<Pensjonsak> hentPensjonssaker(final String personident) {
		if (isBlank(personident)) {
			return emptyList();
		}

		return webClient.get()
				.uri(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl() + "/pen/springapi/sak/sammendrag")
				.attributes(getOAuth2AuthorizedClient())
				.header("fnr", personident)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Pensjonsak>>() {
				})
				.doOnError(handleErrorPensjonssaker())
				.block();
	}

	private Consumer<Throwable> handleErrorPensjonssaker() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				if (NOT_FOUND.equals(((WebClientResponseException) error).getStatusCode())) {
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

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(getOAuth2AuthorizeRequestForAzure(CLIENT_REGISTRATION_PENSJON));
		return oauth2AuthorizedClient(clientMono.block());
	}
}
