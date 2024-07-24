package no.nav.safselvbetjening.fullmektig;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.tokendings.TokenResponse;
import no.nav.safselvbetjening.tokendings.TokendingsConsumer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class FullmektigConsumer {
	static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";

	private final SafSelvbetjeningProperties.TokenXEndpoint pdlfullmakt;
	private final TokendingsConsumer tokendingsConsumer;
	private final WebClient webClient;

	public FullmektigConsumer(WebClient webClient,
							  SafSelvbetjeningProperties safSelvbetjeningProperties,
							  TokendingsConsumer tokendingsConsumer,
							  ObjectMapper objectMapper) {
		this.pdlfullmakt = safSelvbetjeningProperties.getEndpoints().getPdlfullmakt();
		this.tokendingsConsumer = tokendingsConsumer;
		this.webClient = webClient.mutate()
				.baseUrl(pdlfullmakt.getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.exchangeStrategies(ExchangeStrategies.builder().codecs(clientCodecConfigurer ->
								clientCodecConfigurer.customCodecs()
										.register(new Jackson2JsonDecoder(objectMapper, MimeTypeUtils.APPLICATION_JSON)))
						.build())
				.build();
	}

	public List<FullmektigTemaResponse> fullmektigTema(String fullmektigSubjectToken) {
		TokenResponse exchange = tokendingsConsumer.exchange(fullmektigSubjectToken, pdlfullmakt.getScope());
		return webClient.get()
				.uri("/api/fullmektig/tema")
				.headers(h -> {
					h.setBearerAuth(exchange.accessToken());
					h.set(HEADER_NAV_CALL_ID, getCallId());
				})
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<FullmektigTemaResponse>>() {
				})
				.onErrorResume(DecodingException.class, throwable -> {
					log.error("Klarte ikke dekode JSON payload fra /api/fullmektig/tema", throwable);
					return Mono.empty();
				})
				.onErrorResume(WebClientResponseException.class, throwable -> {
					log.error("Kall feilet mot /api/fullmektig/tema, status={}", throwable.getStatusCode(), throwable);
					return Mono.empty();
				})
				.defaultIfEmpty(new ArrayList<>())
				.block();
	}
}
