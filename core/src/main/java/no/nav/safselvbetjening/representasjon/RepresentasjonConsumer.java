package no.nav.safselvbetjening.representasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.representasjon.api.RepresentasjonsforholdDto;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static no.nav.safselvbetjening.MDCUtils.getCallId;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static no.nav.safselvbetjening.consumer.token.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static no.nav.safselvbetjening.consumer.token.NaisTexasRequestInterceptor.TOKEN_FOR_EXCHANGE;

/// Klient for [repr-api](https://repr-api.intern.nav.no/swagger-ui/index.html?urls.primaryName=v2#/Representasjon/hentAktiveRepresentasjonsforholdHvorIdentErRepresentantGruppertPaaDenRepresenterte)
@Slf4j
@Component
public class RepresentasjonConsumer {

	private final SafSelvbetjeningProperties.TokenXEndpoint reprApi;
	private final RestClient restClient;

	public RepresentasjonConsumer(RestClient restClientTexas,
								  SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.reprApi = safSelvbetjeningProperties.getEndpoints().getReprApi();
		this.restClient = restClientTexas.mutate()
				.baseUrl(reprApi.getUrl())
				.build();
	}

	public RepresentasjonsforholdDto representasjonsForhold(String representantSubjectJwt) {
		return restClient.get()
				.uri("/api/v2/eksternbruker/kan-representere")
				.headers(h -> {
					h.set(NAV_CALLID, getCallId());
				})
				.attributes(a -> {
					a.put(TOKEN_FOR_EXCHANGE, representantSubjectJwt);
					a.put(TARGET_SCOPE, reprApi.getScope());
				})
				.exchange((_, res) -> {
					if (res.getStatusCode().isError()) {
						try {
							ProblemDetail problemDetail = res.bodyTo(ProblemDetail.class);
							if (problemDetail != null && problemDetail.getProperties() != null) {
								log.error("Kall feilet mot repr-api kan-representere oppslag, status={}, errorCode={}",
										res.getStatusCode(), problemDetail.getProperties().getOrDefault("errorCode", "null"));
							} else {
								log.error("Kall feilet mot repr-api kan-representere oppslag, status={}", res.getStatusCode());
							}
							return RepresentasjonsforholdDto.empty();
						} catch (Exception e) {
							log.error("Kall feilet mot repr-api kan-representere oppslag, status={}", res.getStatusCode());
							return RepresentasjonsforholdDto.empty();
						}
					}
					try {
						RepresentasjonsforholdDto body = res.bodyTo(RepresentasjonsforholdDto.class);
						if (body == null || body.fullmakt() == null || body.vergemaal() == null) {
							log.error("Klarte ikke deserialisere svar fra repr-api kan-representere oppslag: manglende felt");
							return RepresentasjonsforholdDto.empty();
						}
						return body;
					} catch (Exception e) {
						log.error("Klarte ikke deserialisere svar fra repr-api kan-representere oppslag", e);
						return RepresentasjonsforholdDto.empty();
					}
				});
	}
}