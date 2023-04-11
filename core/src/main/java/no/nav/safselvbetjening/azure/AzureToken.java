package no.nav.safselvbetjening.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static no.nav.safselvbetjening.cache.CacheConfig.AZURE_TOKEN_CACHE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Component
public class AzureToken {

	private final WebClient webClient;
	private final AzureProperties azureProperties;
	private final ObjectMapper objectMapper;

	public AzureToken(WebClient webClient, AzureProperties azureProperties, ObjectMapper objectMapper) {

		this.azureProperties = azureProperties;
		this.objectMapper = objectMapper;
		this.webClient = webClient.mutate()
				.baseUrl(azureProperties.openidConfigTokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
	}

	@Cacheable(value = AZURE_TOKEN_CACHE, key = "#scope")
	@Retryable(retryFor = ConsumerTechnicalException.class, backoff = @Backoff(delay = 2000))
	public String fethAccessToken(String scope) {
		MultiValueMap<String, String> formMultiValueData = new LinkedMultiValueMap<>();
		formMultiValueData.add("client_id", azureProperties.appClientId());
		formMultiValueData.add("client_secret", azureProperties.appClientSecret());
		formMultiValueData.add("grant_type", "client_credentials");
		formMultiValueData.add("scope", scope);

		String responseJson = webClient.post()
				.body(BodyInserters.fromFormData(formMultiValueData))
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();

		try {
			return objectMapper.readValue(responseJson, TokenResponse.class).accessToken();
		} catch (JsonProcessingException e) {
			throw new AzureTokenException(format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AzureTokenException(
					format("Klarte ikke hente token fra Azure. Feilet med statuskode=%s Feilmelding=%s",
							response.getStatusCode().value(),
							response.getMessage()),
					error);
		} else {
			throw new AzureTokenTechnicalException(
					format("Kall mot Azure feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
