package no.nav.safselvbetjening.consumer.pdl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.safselvbetjening.azure.AzureProperties;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * PDL implementasjon av {@link IdentConsumer}
 */
@Component
class PdlIdentConsumer implements IdentConsumer {
	private static final String PDL_INSTANCE = "pdl";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final WebClient webClient;
	private final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

	public PdlIdentConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
							final WebClient webClient,
							final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager
	) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.webClient = webClient;
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
	}

	@Retry(name = PDL_INSTANCE)
	@CircuitBreaker(name = PDL_INSTANCE)
	@Override
	public List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException {

		PdlResponse pdlResponse = webClient.post()
				.uri(safSelvbetjeningProperties.getEndpoints().getPdl().getUrl())
				.attributes(getOAuth2AuthorizedClient())
				.headers(this::createHeaders)
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
			if (error instanceof WebClientResponseException && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
				throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", error);
			}
		};
	}

	private void createHeaders(HttpHeaders headers) {
		headers.setContentType(APPLICATION_JSON);
		headers.set(NAV_CALLID, getCallId());
	}

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(AzureProperties.getOAuth2AuthorizeRequestForAzure());
		return ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(clientMono.block());
	}
}
