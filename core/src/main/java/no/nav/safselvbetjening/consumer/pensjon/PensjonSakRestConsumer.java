package no.nav.safselvbetjening.consumer.pensjon;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.azure.AzureTokenClient;
import no.nav.safselvbetjening.azure.WebClientAzureAuthentication;
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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
			final AzureTokenClient azureTokenClient
	) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.webClient = webClient.mutate()
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(safSelvbetjeningProperties.getEndpoints().getPensjon().getScope(), azureTokenClient))
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
}
