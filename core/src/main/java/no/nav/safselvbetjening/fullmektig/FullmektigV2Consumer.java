package no.nav.safselvbetjening.fullmektig;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.tokendings.TokenResponse;
import no.nav.safselvbetjening.tokendings.TokendingsConsumer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;

@Slf4j
@Component
public class FullmektigV2Consumer {

	private final SafSelvbetjeningProperties.TokenXEndpoint reprApi;
	private final TokendingsConsumer tokendingsConsumer;
	private final RestClient restClient;

	public FullmektigV2Consumer(RestClient restClientTexas,
								SafSelvbetjeningProperties safSelvbetjeningProperties,
								TokendingsConsumer tokendingsConsumer) {
		this.reprApi = safSelvbetjeningProperties.getEndpoints().getReprApi();
		this.tokendingsConsumer = tokendingsConsumer;
		this.restClient = restClientTexas.mutate()
				.baseUrl(reprApi.getUrl())
				.build();
	}

	public List<KanRepresentereDetaljertTemaResponse> fullmektigTema(String fullmektigSubjectToken) {
		TokenResponse exchange = tokendingsConsumer.exchange(fullmektigSubjectToken, reprApi.getScope());
		return restClient.get()
				.uri("/api/v2/eksternbruker/fullmakt/kan-representere")
				.headers(h -> {
					h.setBearerAuth(exchange.accessToken());
					h.set(NAV_CALLID, getCallId());
				})
				.exchange((_, res) -> {
					if (res.getStatusCode().isError()) {
						log.error("Kall feilet mot repr-api fullmektig-oppslag, status={}", res.getStatusCode());
						return List.of();
					}
					try {
						List<KanRepresentereDetaljertTemaResponse> body = res.bodyTo(new ParameterizedTypeReference<>() {
						});
						return body != null ? body : List.of();
					} catch (Exception e) {
						log.error("Klarte ikke deserialisere svar fra repr-api fullmektig-oppslag", e);
						return List.of();
					}
				});
	}
}
