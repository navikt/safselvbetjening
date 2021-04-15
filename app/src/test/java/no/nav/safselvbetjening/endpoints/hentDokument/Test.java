package no.nav.safselvbetjening.endpoints.hentDokument;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.Application;
import no.nav.safselvbetjening.ApplicationConfig;
import no.nav.safselvbetjening.endpoints.STSTestConfig;
import no.nav.safselvbetjening.endpoints.TestSecurityConfig;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.EnableJwtTokenValidationConfiguration;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class, ApplicationConfig.class, STSTestConfig.class, TestSecurityConfig.class, EnableJwtTokenValidationConfiguration.class})
@EnableMockOAuth2Server
@ActiveProfiles("itest")
public class Test {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private MockOAuth2Server server;

	@BeforeEach
	void initialiseRestAssuredMockMvcWebApplicationContext() {
		Collection<Filter> filterCollection = webApplicationContext.getBeansOfType(Filter.class).values();
		Filter[] filters = filterCollection.toArray(new Filter[0]);
		MockMvcConfigurer mockMvcConfigurer = new MockMvcConfigurer() {
			@Override
			public void afterConfigurerAdded(ConfigurableMockMvcBuilder<?> builder) {
				builder.addFilters(filters);
			}
		};
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext, mockMvcConfigurer);
	}

	@org.junit.Test
	public void validTokenInRequestMultipleIssuers() {
		String token1 = token("tokenx", "subject1", "demoapplication");
		String uri = "/rest/hentdokument/123/123/ARKIV";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token1);

	}
/*
	protected HttpEntity<?> createHttpEntity() {
		return new HttpEntity<>(createHeaders());
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token(mockOAuth2Server, "tokenx", "Subject", "audience"));

		//headers.add(HttpHeaders.AUTHORIZATION, getTokenWithSubejct(PERSON_USER_ID));
		return headers;
	}*/

	private String token(String issuerId, String subject, String audience) {
		return server.issueToken(
				issuerId,
				"theclientid",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
						List.of(audience),
						Collections.emptyMap(),
						3600
				)
		).serialize();
	}
	/*

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private WireMockServer wireMockServer;

	@LocalServerPort
	private String webAppPort;

	@Autowired
	private MockOAuth2Server mockOAuth2Server;
	@Autowired
	protected TestRestTemplate restTemplate;
	@Autowired
	protected HentDokumentController hentDokumentController;

	@Value("${wiremock.port}")
	Integer wiremockPort;

	private final SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();


	@BeforeEach
	public void setup() {

		Collection<Filter> filterCollection = webApplicationContext.getBeansOfType(Filter.class).values();
		Filter[] filters = filterCollection.toArray(new Filter[0]);
		MockMvcConfigurer mockMvcConfigurer = new MockMvcConfigurer() {
			@Override
			public void afterConfigurerAdded(ConfigurableMockMvcBuilder<?> builder) {
				builder.addFilters(filters);
			}
		};
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext, mockMvcConfigurer);

		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));

		this.wireMockServer = new WireMockServer(
				options()
						.extensions(new ResponseTemplateTransformer(false))
						.port(wiremockPort));
		this.wireMockServer.start();
	}

	@AfterEach
	public void afterEach() {
		this.wireMockServer.stop();
	}

	public static String token(MockOAuth2Server mockOAuth2Server, String issuerId, String subject, String audience, String... groups) {
		return mockOAuth2Server.issueToken(
				issuerId,
				"theclientid",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
						List.of(audience),
						Collections.singletonMap("groups", Arrays.asList(groups)),
						3600
				)
		).serialize();
	}

	@org.junit.Test
	public void happyPath() {

		boolean friend = true;

		assertTrue(friend);

		createHttpEntity();
		/*HttpResponse<String> response = newBuilder().build().send(
				HttpRequest.newBuilder()
						.uri(URI.create("http://localhost:" + webAppPort + "/altinn-meldinger-api/melding"))
						.header("Content-Type", "application/json")
						.header("Authorization", "Bearer " + token(mockOAuth2Server, "aad", "subject", "altinn-meldinger-api", "rettighet-for-å-bruke-apiet-lokalt"))
						.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(altinnMelding)))
						.build(),
				ofString()
		);*/

	/*stubHentjournalsakinfo();
	stubHenttilgangJournalpost("/fagarkiv/tilgangJournalpostResponse.json");

	ResponseEntity<String> responseEntity = callHentDokument();

	assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());

	verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));

}
*/
/*
	private ResponseEntity<String> callHentDokument() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		return restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}

	private void stubHenttilgangJournalpost(String fil) {
		wireMockServer.stubFor(get(urlEqualTo("henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile(fil)));
	}

	private void stubHentjournalsakinfo() {
		wireMockServer.stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));
	}
*/

}
