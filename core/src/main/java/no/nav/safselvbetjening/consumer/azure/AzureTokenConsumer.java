package no.nav.safselvbetjening.consumer.azure;

import no.nav.safselvbetjening.AzureProperties;
import no.nav.safselvbetjening.cache.CacheConfig;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;


@Component
public class AzureTokenConsumer {
    private final RestTemplate restTemplate;
    private final AzureProperties azureProperties;

    public AzureTokenConsumer(AzureProperties azureProperties,
                              RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
        this.azureProperties = azureProperties;
    }

    @Retryable(include = AzureTokenException.class)
    @Cacheable(CacheConfig.AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE)
    public TokenResponse getClientCredentialToken() {
        try {
            HttpHeaders headers = createHeaders();
            String form = "grant_type=client_credentials&scope=" + azureProperties.getScope() + "&client_id=" +
                    azureProperties.getClientId() + "&client_secret=" + azureProperties.getClientSecret();
            HttpEntity<String> requestEntity = new HttpEntity<>(form, headers);

            return restTemplate.exchange(azureProperties.getTokenUrl(), HttpMethod.POST, requestEntity, TokenResponse.class)
                    .getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new AzureTokenException(String.format("Klarte ikke hente token fra Azure. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}