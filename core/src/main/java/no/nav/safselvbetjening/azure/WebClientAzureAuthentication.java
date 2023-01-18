package no.nav.safselvbetjening.azure;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;

public record WebClientAzureAuthentication(String scope, AzureToken azureToken) implements ExchangeFilterFunction {

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> {
					httpHeaders.setBearerAuth(azureToken.fethAccessToken(scope));
					httpHeaders.set(NAV_CALLID, getCallId());
				})
				.build());
	}
}
