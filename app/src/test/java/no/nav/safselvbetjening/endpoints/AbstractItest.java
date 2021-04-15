package no.nav.safselvbetjening.endpoints;

import no.nav.safselvbetjening.ApplicationConfig;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.EnableJwtTokenValidationConfiguration;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationConfig.class, EnableJwtTokenValidationConfiguration.class, STSTestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "itest")
//@ImportAutoConfiguration
//@EnableAutoConfiguration
//@AutoConfigureWireMock(port = Options.DYNAMIC_PORT)
@EnableMockOAuth2Server
//@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc"})
public abstract class AbstractItest {

	/*
	@Autowired
	protected TestRestTemplate restTemplate;
	@Autowired
	protected HentDokumentController hentDokumentController;
*/
	@Value("${wiremock.port}")
	Integer wiremockPort;

	@LocalServerPort
	private String webAppPort;

	@Autowired
	private MockOAuth2Server mockOAuth2Server;

	//private WireMockServer wireMockServer;

	//private final SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();

	protected static final String PERSON_USER_ID = "Z990782";


	@BeforeEach
	public void setup() {
		//safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));

		/*wireMockServer = new WireMockServer(
				options()
						.extensions(new ResponseTemplateTransformer(false))
						.port(wiremockPort));
		wireMockServer.start();*/

		/*
		Collection<Filter> filterCollection = webApplicationContext.getBeansOfType(Filter.class).values();
		Filter[] filters = filterCollection.toArray(new Filter[0]);
		MockMvcConfigurer mockMvcConfigurer = new MockMvcConfigurer() {
			@Override
			public void afterConfigurerAdded(ConfigurableMockMvcBuilder<?> builder) {
				builder.addFilters(filters);
			}
		};
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext, mockMvcConfigurer);*/
	}
	/*
	protected HttpEntity<?> createHttpEntity() {
		return new HttpEntity<>(createHeaders());
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(NAV_CALLID, "itest");
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token("tokenx", "Subject", "audience"));

		//headers.add(HttpHeaders.AUTHORIZATION, getTokenWithSubejct(PERSON_USER_ID));
		return headers;
	}*/


	/*private String getTokenWithSubejct(final String subject) {
		return "Bearer " + restTemplate.getForObject("/local/jwt?subject=" + subject, String.class);
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
/*
	@AfterEach
	public void tearDown() {
		wireMockServer.stop();
	}*/
}
