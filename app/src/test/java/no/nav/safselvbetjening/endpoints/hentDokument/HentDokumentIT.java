package no.nav.safselvbetjening.endpoints.hentDokument;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode.ARKIV;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKJULT_INNSYN;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@EmbeddedKafka(
		topics = {"test-ut-topic"},
		bootstrapServersProperty = "spring.kafka.bootstrap-servers",
		partitions = 1
)
class HentDokumentIT extends AbstractItest {

	@Value("${safselvbetjening.topics.dokdistdittnav}")
	public static String UT_TOPIC = "test-ut-topic";

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public EmbeddedKafkaBroker kafkaEmbedded;

	public static Consumer<String, HoveddokumentLest> consumer;

	private static final String DOKUMENT_ID = "123";
	private static final String JOURNALPOST_ID = "123";
	private static final String BRUKER_ID = "12345678911";
	private static final VariantFormatCode VARIANTFORMAT = ARKIV;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@BeforeEach
	public void setUpClass() {
		// KafkaConsumer for å kunne konsumere meldinger som InngaaendeHendelsePublisher dytter til 'test-ut-topic'
		this.setUpConsumerForTopicUt();
	}

	public void setUpConsumerForTopicUt() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test", "true", kafkaEmbedded);
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
		consumerProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
		consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

		consumer = new DefaultKafkaConsumerFactory<String, HoveddokumentLest>(consumerProps).createConsumer();
		consumer.subscribe(singletonList(UT_TOPIC));
	}

	public List<HoveddokumentLest> getAllCurrentRecordsOnTopicUt() {
		return StreamSupport.stream(KafkaTestUtils.getRecords(consumer, ofSeconds(2)).records(UT_TOPIC).spliterator(), false)
				.map(ConsumerRecord::value)
				.collect(Collectors.toList());
	}

	@Test
	void hentFerdigstiltDokumentHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void shouldHentDokumentWhenSubToken() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentSubToken();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentMidlertidigDokumentHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_midlertidig_happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentWhenInnsynIsVises() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_innsynvises.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentNotFound() {
		stubPdl();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();
		stubFor(get("/fagarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnBadRequestWhenJournalpostIdNotNumeric() {
		stubAzure();

		String uri = "/rest/hentdokument/123456a/" + DOKUMENT_ID + "/" + VARIANTFORMAT;
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, GET, createHttpEntityHeaders(BRUKER_ID), String.class);

		assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	void hentTilgangJournalpostNotFound() {
		stubPdl();
		stubAzure();
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentDokarkivTechnicalFail() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentPdlNotFound() {
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();
		stubHentDokumentDokarkiv();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl_not_found.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldHentDokumentTilgangAvvistInnsynSkjules() {
		stubPdl();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_innsyn_skjules.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_SKJULT_INNSYN));
	}

	@Test
	void hentDokumentTilgangAvvist() {
		stubPdl();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_gdpr.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_GDPR));
	}

	@Test
	void hentDokumentPenHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubPensjonHentBrukerForSak("hentbrukerforsak_happy.json");
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_pen_happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertOkArkivResponse(responseEntity);
	}


	@Test
	void hentDokumentUtgaaendePenKafkaHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubPensjonHentBrukerForSak("hentbrukerforsak_happy.json");
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_utgaaende_pen_happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertOkArkivResponse(responseEntity);

		//Consumer topic og verifiser 1 melding
		await().atMost(10, SECONDS).untilAsserted(() -> {
			List<HoveddokumentLest> records = this.getAllCurrentRecordsOnTopicUt();
			assertEquals(1, records.size());
			HoveddokumentLest hoveddokumentLest = records.get(0);
			assertEquals(JOURNALPOST_ID, hoveddokumentLest.getJournalpostId());
			assertEquals(DOKUMENT_ID, hoveddokumentLest.getDokumentInfoId());
		});
	}

	@Test
	void hentDokumentUtgaaendePenIkkeKafkaHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_pen_happy.json");
		stubPensjonHentBrukerForSak("hentbrukerforsak_happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertOkArkivResponse(responseEntity);
		await().timeout(ofSeconds(5))
				.failFast("Kafka topicen skal ikke ha size=1 for denne testen", () -> getAllCurrentRecordsOnTopicUt().size() >= 1)
				.until(() -> getAllCurrentRecordsOnTopicUt().size() == 0);
	}

	@Test
	void hentDokumentPenNotFound() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubPensjonHentBrukerForSak("hentbrukerforsak_empty.json");
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_pen_happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnUnauthorizedWhenLokalprintSkannet() {
		stubPdl();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_lokalprint_skannet.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_SKANNET_DOKUMENT));
	}

	// Fullmakt relatert
	@Test
	void shouldHentDokumentWhenTokenNotMatchingQueryIdentAndFullmaktExistsForTema() {
		stubPdlFullmakt("pdl_fullmakt_aap.json");
		stubPdl();
		stubTokenx();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertOkArkivResponse(responseEntity);
		await().timeout(ofSeconds(5))
				.failFast("Kafka topicen skal ikke ha size=1 for denne testen", () -> getAllCurrentRecordsOnTopicUt().size() >= 1)
				.until(() -> getAllCurrentRecordsOnTopicUt().size() == 0);
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktIkkeGittForTema() {
		stubPdl();
		stubPdlFullmakt("pdl_fullmakt_for.json");
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndNoFullmakt() {
		stubPdl();
		stubPdlFullmakt();
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndWrongFullmakt() {
		stubPdl();
		stubPdlFullmakt("pdl_fullmakt_feil_bruker.json");
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturns4xx() {
		stubPdl();
		stubPdlFullmakt(FORBIDDEN);
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturns5xx() {
		stubPdl();
		stubPdlFullmakt(INTERNAL_SERVER_ERROR);
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturnsInvalidJson() {
		stubPdl();
		stubPdlFullmakt("pdl_fullmakt_invalid_json.json");
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturnsInvalidJsonNoArray() {
		stubPdl();
		stubPdlFullmakt("pdl_fullmakt_invalid_json_no_array.json");
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndIngenFullmaktOmraader() {
		stubPdl();
		stubPdlFullmakt("pdl_fullmakt_ingen_omraader.json");
		stubTokenx();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
	}

	private void stubHentDokumentDokarkiv() {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(TEST_FILE_BYTES)));
	}

	private void stubHentTilgangJournalpostDokarkiv() {
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_ferdigstilt_happy.json")));
	}

	private void stubHentTilgangJournalpostDokarkiv(final String file) {
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/" + file)));
	}

	private void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(APPLICATION_PDF, responseEntity.getHeaders().getContentType());
		assertEquals("inline", responseEntity.getHeaders().getContentDisposition().getType());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());
		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private ResponseEntity<String> callHentDokument() {
		return callHentDokument(BRUKER_ID);
	}

	private ResponseEntity<String> callHentDokumentAsFullmektig() {
		return callHentDokument(FULLMEKTIG_ID);
	}

	private ResponseEntity<String> callHentDokument(String innloggetBrukerId) {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT;
		return this.restTemplate.exchange(uri, GET, createHttpEntityHeaders(innloggetBrukerId), String.class);
	}

	private ResponseEntity<String> callHentDokumentSubToken() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT;
		return this.restTemplate.exchange(uri, GET, createHttpEntityHeadersSubToken(BRUKER_ID), String.class);
	}

}
