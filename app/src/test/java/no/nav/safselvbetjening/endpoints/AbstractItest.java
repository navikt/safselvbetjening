package no.nav.safselvbetjening.endpoints;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.safselvbetjening.Application;
import no.nav.safselvbetjening.ApplicationConfig;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, ApplicationConfig.class, STSTestConfig.class})
@EnableMockOAuth2Server
@ActiveProfiles("itest")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractItest {

	@Autowired
	private MockOAuth2Server server;
	@Autowired
	protected TestRestTemplate restTemplate;

	private final SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();


	@BeforeEach
	void initialiseRestAssuredMockMvcWebApplicationContext() {
		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		WireMock.resetAllScenarios();
	}

	protected String token(String subject) {
		String issuerId = "tokenx";
		String audience = "safselvbetjening";
		Map<String, Object> claims = new HashMap<>();
		claims.put("pid", subject);
		return server.issueToken(
				issuerId,
				"safselvbetjening",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
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
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token(subject));
		return new HttpEntity<>(headers);
	}
}
