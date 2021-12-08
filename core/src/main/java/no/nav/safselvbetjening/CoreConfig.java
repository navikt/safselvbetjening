package no.nav.safselvbetjening;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

import java.net.URI;

@ComponentScan
@EnableRetry
@EnableJwtTokenValidation
@EnableKafka
@Configuration
public class CoreConfig {
	@Bean
	ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	@Bean
	HttpClient httpClient(HttpClientConnectionManager connectionManager) {
		return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	@Bean
	HttpClientConnectionManager httpClientConnectionManager() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(100);
		return connectionManager;
	}

	@Bean
	MeterFilter meterFilter(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		return new MeterFilter() {
			@Override
			public MeterFilterReply accept(Meter.Id id) {
				// Hindre sak metrikker fra å registreres i prometheus
				if (id.getName().startsWith("http.client.requests")
						&& id.getTag("clientName") != null
						&& id.getTag("clientName").startsWith(URI.create(safSelvbetjeningProperties.getEndpoints().getSak()).getHost())) {
					return MeterFilterReply.DENY;
				}
				return MeterFilterReply.ACCEPT;
			}
		};
	}
}
