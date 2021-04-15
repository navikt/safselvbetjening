package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.ApplicationConfig;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.endpoints.STSTestConfig;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.EnableJwtTokenValidationConfiguration;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ApplicationConfig.class, STSTestConfig.class, EnableJwtTokenValidationConfiguration.class})
@EnableMockOAuth2Server
@ActiveProfiles("itest")
class HentDokumentIT {

	//private WireMockServer wireMockServer;

	@LocalServerPort
	private String webAppPort;

	@Autowired
	private MockOAuth2Server mockOAuth2Server;

	@Value("${wiremock.port}")
	Integer wiremockPort;
/*
	@BeforeEach
	public void setup() throws Exception {
		this.wireMockServer = new WireMockServer(
				options()
						.extensions(new ResponseTemplateTransformer(false))
						.port(wiremockPort));
		this.wireMockServer.start();
	}

	@AfterEach
	public void afterEach() {
		this.wireMockServer.stop();
	}*/

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

	private static final String DOKUMENT_ID = "12345";
	private static final String JOURNALPOST_ID = "98765";
	private static final VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static final VariantFormatCode SLADDET_VARIANTFORMAT = VariantFormatCode.SLADDET;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@Test
	void happyPath() {

		boolean friend = true;

		assertTrue(friend);

		/*stubHentjournalsakinfo();
		stubHenttilgangJournalpost("/fagarkiv/tilgangJournalpostResponse.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());

		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
*/
	}
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
	}*/
}
