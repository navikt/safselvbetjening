package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.azure.AzureToken;
import no.nav.safselvbetjening.azure.WebClientAzureAuthentication;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private static final String PENSJON_INSTANCE_BRUKER_FOR_SAK = "pensjonbrukerforsak";
	private static final String PENSJON_INSTANCE_PENSJONSSAKER = "pensjonpensjonssaker";

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final WebClient webClient;

	public PensjonSakRestConsumer(
			final SafSelvbetjeningProperties safSelvbetjeningProperties,
			final WebClient webClient,
			final AzureToken azureToken
	) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.webClient = webClient.mutate()
				.filter(new WebClientAzureAuthentication(safSelvbetjeningProperties.getEndpoints().getPensjon().getScope(), azureToken))
				.build();

	}

	@Retry(name = PENSJON_INSTANCE_BRUKER_FOR_SAK)
	@CircuitBreaker(name = PENSJON_INSTANCE_BRUKER_FOR_SAK)
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {

		var result = webClient.get()
				.uri(safSelvbetjeningProperties.getEndpoints().getPensjon().getUrl() + "/pen/api/pip/hentBrukerOgEnhetstilgangerForSak/v1")
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
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				throw new ConsumerFunctionalException(String.format("hentBrukerForSak feilet funksjonelt med statuskode=%s. Feilmelding=%s", response.getStatusCode(), response.getMessage()), error);
			} else {
				throw new ConsumerTechnicalException(String.format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
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
				.header("fnr", personident)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body -> {
					if (clientResponse.statusCode() == NOT_FOUND) {
						return Mono.error(new ConsumerFunctionalException(
								String.format("hentPensjonssaker feilet funksjonelt (person ikke funnet). Statuskode=%s. Feilmelding=%s", clientResponse.statusCode(), body)
						));
					}
					return Mono.error(new ConsumerFunctionalException(
							String.format("hentPensjonssaker feilet funksjonelt med statuskode=%s. Feilmelding=%s", clientResponse.statusCode(), body)
					));
				}))
				.bodyToMono(new ParameterizedTypeReference<List<Pensjonsak>>() {
				})
				.doOnError(handleErrorPensjonssaker())
				.block();
	}

	private Consumer<Throwable> handleErrorPensjonssaker() {
		return error -> {
			if (error instanceof ConsumerFunctionalException response) {
				throw response;
			} else {
				throw new ConsumerTechnicalException(String.format("hentPensjonssaker feilet teknisk. Feilmelding=%s", error.getMessage()), error);
			}
		};
	}
}
