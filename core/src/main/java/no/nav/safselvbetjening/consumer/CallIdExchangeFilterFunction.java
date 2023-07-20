package no.nav.safselvbetjening.consumer;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.safselvbetjening.MDCUtils.getCallId;

public class CallIdExchangeFilterFunction implements ExchangeFilterFunction {

	private final String callIdHeadername;

	public CallIdExchangeFilterFunction(String callIdHeadername) {
		this.callIdHeadername = callIdHeadername;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		return next.exchange(ClientRequest.from(request)
				.headers(httpHeaders -> httpHeaders.set(callIdHeadername, getCallId()))
				.build());
	}
}
