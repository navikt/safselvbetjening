package no.nav.safselvbetjening.consumer.pdl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.azure.AzureToken;
import no.nav.safselvbetjening.azure.WebClientAzureAuthentication;
import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * PDL implementasjon av {@link IdentConsumer}
 */
@Component
class PdlIdentConsumer implements IdentConsumer {
	private static final String PDL_INSTANCE = "pdl";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final WebClient webClient;

	public PdlIdentConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final WebClient webClient,
							final AzureToken azureToken
	) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.webClient = webClient.mutate()
				.filter(new WebClientAzureAuthentication(safSelvbetjeningProperties.getEndpoints().getPdl().getScope(), azureToken))
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retry(name = PDL_INSTANCE)
	@CircuitBreaker(name = PDL_INSTANCE)
	@Override
	public List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException {

		PdlResponse pdlResponse = webClient.post()
				.uri(safSelvbetjeningProperties.getEndpoints().getPdl().getUrl())
				.bodyValue(mapHentIdenterQuery(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.doOnError(handleErrorPdl())
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentIdenter().getIdenter();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
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

	private Consumer<Throwable> handleErrorPdl() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", error);
			}
		};
	}

}
