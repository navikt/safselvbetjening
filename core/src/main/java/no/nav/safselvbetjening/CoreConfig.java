package no.nav.safselvbetjening;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;

import static io.micrometer.core.instrument.config.MeterFilterReply.ACCEPT;
import static io.micrometer.core.instrument.config.MeterFilterReply.DENY;
import static org.apache.hc.core5.util.Timeout.ofSeconds;

@Slf4j
@EnableRetry
@EnableJwtTokenValidation
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
@Configuration
public class CoreConfig {

	public static final Clock SYSTEM_CLOCK = Clock.system(ZoneId.of("Europe/Oslo"));

	@Bean
	ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
		var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		requestFactory.setConnectTimeout(5_000);

		return requestFactory;
	}

	@Bean
	HttpClient httpClient(HttpClientConnectionManager httpClientConnectionManager) {
		return HttpClients.custom()
				.setConnectionManager(httpClientConnectionManager)
				.build();
	}

	@Bean
	HttpClientConnectionManager httpClientConnectionManager() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

		var readTimeout = SocketConfig.custom().setSoTimeout(ofSeconds(60)).build();
		connectionManager.setDefaultSocketConfig(readTimeout);
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
					&& id.getTag("clientName").startsWith(URI.create(safSelvbetjeningProperties.getEndpoints().getSak().getUrl()).getHost())) {
					return DENY;
				}
				return ACCEPT;
			}
		};
	}

	@Bean
	UtledTilgangService utledTilgangService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		return new UtledTilgangService();
	}
}
