package no.nav.safselvbetjening.consumer;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.springframework.http.MediaType.TEXT_HTML;

public final class ConsumerExceptionHandlers {
	private ConsumerExceptionHandlers() {
		// noop
	}

	public static void handleMidlertidigNginxError(WebClientResponseException webClientResponseException) {
		if(webClientResponseException instanceof WebClientResponseException.NotFound notFound) {
			handleMidlertidigNginxError(notFound);
		}
	}

	public static void handleMidlertidigNginxError(WebClientResponseException.NotFound notFound) {
		String responseBody = notFound.getResponseBodyAs(String.class);
		if (isNginxResponse(notFound, responseBody)) {
			throw new NginxException("Midlertidig feil mot nginx loadbalancer. Forsøker retry", notFound);
		}
	}

	private static boolean isNginxResponse(WebClientResponseException.NotFound notFound, String responseBody) {
		return responseBody != null && responseBody.contains("nginx") && TEXT_HTML.equals(notFound.getHeaders().getContentType());
	}
}
