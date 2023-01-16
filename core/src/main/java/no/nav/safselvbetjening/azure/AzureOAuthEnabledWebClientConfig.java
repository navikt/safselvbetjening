package no.nav.safselvbetjening.azure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Configuration
public class AzureOAuthEnabledWebClientConfig {

	@Bean
	WebClient webClient(WebClient.Builder webClientBuilder) {

		var nettyHttpClient = HttpClient.create()
				.responseTimeout(Duration.of(20, SECONDS));
		var clientHttpConnector = new ReactorClientHttpConnector(nettyHttpClient);

		return webClientBuilder.clone()
				.clientConnector(clientHttpConnector)
				.build();
	}
}
