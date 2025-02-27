package no.nav.safselvbetjening.endpoints;

import no.nav.safselvbetjening.Application;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.apache.zookeeper.common.StringUtils.isEmpty;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {Application.class}
)
@EnableMockOAuth2Server
@ActiveProfiles("itest")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractItest {

	protected static final String FULLMEKTIG_ID = "22222222222";
	protected static final String HENT_PENSJONSSAKER_PATH = "/pensjon/api/sak/sammendrag";
	protected static final String HENT_BRUKER_FOR_PENSJONSSAK_PATH = "/pensjon/api/pip/hentBrukerOgEnhetstilgangerForSak/v1";

	@Autowired
	private MockOAuth2Server server;
	@Autowired
	protected TestRestTemplate restTemplate;

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
		headers.setContentType(APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.setBearerAuth(pidToken(subject));
		return new HttpEntity<>(headers);
	}

	protected HttpEntity<?> createHttpEntityHeadersSubToken(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.setBearerAuth(subToken(subject));
		return new HttpEntity<>(headers);
	}

	protected HttpHeaders httpHeaders(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.setBearerAuth(pidToken(subject));
		return headers;
	}

	protected HttpHeaders httpHeadersSubToken(String subject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.setBearerAuth(subToken(subject));
		return headers;
	}

	protected String stringFromClasspath(String resourcename) {
		try {
			return IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(resourcename)), UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Klarte ikke lese fil=" + resourcename + " fra classpath til string", e);
		}
	}

	protected void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	protected void stubTokenx() {
		stubFor(post("/tokenx")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	protected void stubPdlGenerell() {
		stubPdl("pdl-generell.json");
	}

	protected void stubPdl(final String fil) {
		stubFor(post("/pdl")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + fil)));
	}

	protected void stubSak() {
		stubSak("saker_happy.json");
	}

	protected void stubSak(final String fil) {
		stubFor(get(urlMatching("/sak\\?aktoerId=1012345678911\\&tema=.*"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sak/" + fil)));
	}

	protected void stubPensjonssaker() {
		stubPensjonssaker("hentpensjonssaker_happy.json");
	}

	protected void stubPensjonssaker(final String fil) {
		stubFor(get(HENT_PENSJONSSAKER_PATH)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/" + fil)));
	}

	protected void stubPensjonHentBrukerForSak() {
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
	}

	protected void stubPensjonHentBrukerForSak(final String fil) {
		stubFor(get(HENT_BRUKER_FOR_PENSJONSSAK_PATH)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/" + fil)));
	}

	protected void stubFagarkiv() {
		stubFagarkiv("finnjournalposter_happy_gsak.json", "finnjournalposter_happy_psak.json");
	}

	protected void stubFagarkiv(final String gsakFil){
		stubFagarkiv(gsakFil, "finnjournalposter_empty.json");
	}

	protected void stubFagarkiv(final String gsakFil, String psakFil) {
		stubFor(post("/dokarkiv/finnjournalposter")
				.withRequestBody(matchingJsonPath("$[?(@.gsakSakIds.size() != null )]"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokarkiv/finnjournalposter/" + gsakFil)));
		if (!isEmpty(psakFil)) {
			stubFor(post("/dokarkiv/finnjournalposter")
					.withRequestBody(matchingJsonPath("$[?(@.psakSakIds.size() != null )]"))
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBodyFile("dokarkiv/finnjournalposter/" + psakFil)));
		}
	}

	protected void stubReprApiFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-empty.json");
	}

	protected void stubReprApiFullmakt(final String fil) {
		stubFor(get("/repr-api/api/eksternbruker/fullmakt/fullmektig/tema")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("repr-api/" + fil)));
	}

	protected void stubReprApiFullmakt(HttpStatus httpStatus) {
		stubFor(get("/repr-api/api/eksternbruker/fullmakt/fullmektig/tema")
				.willReturn(aResponse()
						.withStatus(httpStatus.value())
						.withBody("error " + httpStatus)));
	}
}
