package no.nav.safselvbetjening.fullmektig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.tokendings.TokendingsConsumer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class FullmektigConsumer {
	private final SafSelvbetjeningProperties.TokenXEndpoint pdlfullmakt;
	private final TokendingsConsumer tokendingsConsumer;
	private final WebClient webClient;
	private final ObjectMapper objectMapper;

	public FullmektigConsumer(WebClient webClient, SafSelvbetjeningProperties safSelvbetjeningProperties, TokendingsConsumer tokendingsConsumer, ObjectMapper objectMapper) {
		this.pdlfullmakt = safSelvbetjeningProperties.getEndpoints().getPdlfullmakt();
		this.tokendingsConsumer = tokendingsConsumer;
		this.webClient = webClient.mutate()
				.baseUrl(pdlfullmakt.getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
		this.objectMapper = objectMapper;
	}

	public List<FullmektigTemaResponse> fullmektigTema(String fullmektigSubjectToken) {
		String exchange = tokendingsConsumer.exchange(fullmektigSubjectToken, pdlfullmakt.getScope());
		String responseJson = webClient.get()
				.uri("/api/fullmektig/tema")
				.headers(h -> h.setBearerAuth(exchange))
				.retrieve()
				.bodyToMono(String.class)
				.onErrorResume(WebClientResponseException.class, throwable -> {
					log.error("Kall feilet mot /api/fullmektig/tema, status={}, body={}", throwable.getStatusCode(), throwable.getResponseBodyAsString());
					return Mono.empty();
				})
				.defaultIfEmpty("[]")
				.block();
		try {
			return objectMapper.readValue(responseJson, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			throw new ConsumerFunctionalException(format("Klarte ikke parse svar fra /api/fullmektig/tema. Feilmelding=%s", e.getMessage()), e);
		}
	}
}
