package no.nav.safselvbetjening;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class HttpUtils {

	public static ClientHttpRequestFactory createrequestFactory(Timeout timeout) {
		return new HttpComponentsClientHttpRequestFactory(createHttpClient(timeout));
	}

	private static HttpClient createHttpClient(Timeout timeout) {
		return HttpClients.custom()
				.setConnectionManager(createHttpClientConnectionManager(timeout))
				.build();
	}

	private static HttpClientConnectionManager createHttpClientConnectionManager(Timeout timeout) {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build());
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(100);
		return connectionManager;
	}
}
