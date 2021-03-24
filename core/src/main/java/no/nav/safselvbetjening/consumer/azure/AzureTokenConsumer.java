package no.nav.safselvbetjening.consumer.azure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.safselvbetjening.AzureProperties;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.cache.CacheConfig;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@Component
public class AzureTokenConsumer {
	private static final String AZURE_TOKEN_INSTANCE = "azuretoken";
	private final RestTemplate restTemplate;
	private final AzureProperties azureProperties;

	public AzureTokenConsumer(SafSelvbetjeningProperties safSelvbetjeningProperties,
							  AzureProperties azureProperties,
							  RestTemplateBuilder restTemplateBuilder) {
		final CloseableHttpClient httpClient = createHttpClient(safSelvbetjeningProperties.getProxy());
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
				.build();
		this.azureProperties = azureProperties;
	}

	private CloseableHttpClient createHttpClient(SafSelvbetjeningProperties.Proxy proxy) {
		if(proxy.isSet()) {
			final HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
			return HttpClients.custom()
					.setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost))
					.build();
		} else {
			return HttpClients.createDefault();
		}
	}

	@Retry(name = AZURE_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_TOKEN_INSTANCE)
	@Cacheable(CacheConfig.AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE)
	public TokenResponse getClientCredentialToken() {
		try {
			HttpHeaders headers = createHeaders();
			String form = "grant_type=client_credentials&scope=" + azureProperties.getScope() + "&client_id=" +
					azureProperties.getClientId() + "&client_secret=" + azureProperties.getClientSecret();
			HttpEntity<String> requestEntity = new HttpEntity<>(form, headers);

			return restTemplate.exchange(azureProperties.getTokenUrl(), POST, requestEntity, TokenResponse.class)
					.getBody();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new AzureTokenException(String.format("Klarte ikke hente token fra Azure. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(APPLICATION_JSON));
		return headers;
	}
}