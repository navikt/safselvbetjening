package no.nav.safselvbetjening.fullmektig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.azure.AzureTokenException;
import no.nav.safselvbetjening.tokendings.TokendingsConsumer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

	public List<FullmaktDetails> fullmektig(String fullmektigSubjectToken) {
		String exchange = tokendingsConsumer.exchange(fullmektigSubjectToken, pdlfullmakt.getScope());
		String responseJson = webClient.get()
				.uri("/api/fullmektig")
				.headers(h -> h.setBearerAuth(exchange))
				.retrieve()
				.bodyToMono(String.class)
				.block();

		try {
			return objectMapper.readValue(responseJson, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			throw new AzureTokenException(format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}
}
