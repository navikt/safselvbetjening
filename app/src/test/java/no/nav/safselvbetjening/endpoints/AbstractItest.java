package no.nav.safselvbetjening.endpoints;

import no.nav.safselvbetjening.Application;
import no.nav.safselvbetjening.consumer.azure.TokenConsumer;
import no.nav.safselvbetjening.consumer.azure.TokenResponse;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {Application.class, STSTestConfig.class, AbstractItest.Config.class})
@EnableMockOAuth2Server
@ActiveProfiles("itest")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractItest {

	@Autowired
	private MockOAuth2Server server;
	@Autowired
	protected TestRestTemplate restTemplate;

	static class Config {
		@Bean
		@Primary
		TokenConsumer azureTokenConsumer() {
			return () -> TokenResponse.builder()
					.access_token("dummy")
					.build();
		}
	}

	protected String pidToken(String subject) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("pid", subject);
		return token(subject, claims);
	}

	protected String subToken(String subject) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", subject);
		return token(subject, claims);
	}

	protected String token(String subject, Map<String, Object> claims) {
		String issuerId = "tokenx";
		String audience = "safselvbetjening";
		return server.issueToken(
				issuerId,
				"safselvbetjening",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
						"JWT",
						List.of(audience),
						claims,
						3600
				)
		).serialize();
	}

	protected HttpEntity<?> createHttpEntityHeaders(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + pidToken(subject));
		return new HttpEntity<>(headers);
	}

	protected HttpEntity<?> createHttpEntityHeadersSubToken(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + subToken(subject));
		return new HttpEntity<>(headers);
	}

	protected HttpHeaders httpHeaders(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + pidToken(subject));
		return headers;
	}

	protected HttpHeaders httpHeadersSubToken(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + subToken(subject));
		return headers;
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}

	protected void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	protected void stubPdl() {
		stubPdl("pdl_happy.json");
	}

	protected void stubPdl(final String fil) {
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + fil)));
	}

	protected void stubSak() {
		stubSak("saker_happy.json");
	}

	protected void stubSak(final String fil) {
		stubFor(get(urlMatching("/sak\\?aktoerId=1012345678911\\&tema=.*"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sak/" + fil)));
	}

	protected void stubPensjonSak() {
		stubPensjonSak("pensjonsak_happy.xml");
	}

	protected void stubPensjonSak(final String fil) {
		stubFor(post("/pensjon")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBodyFile("pensjonsak/" + fil)));
	}

	protected void stubFagarkiv() {
		stubFagarkiv("finnjournalposter_happy.json");
	}

	protected void stubFagarkiv(final String fil) {
		stubFor(post("/fagarkiv/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/" + fil)));
	}
}
