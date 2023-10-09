package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode.ARKIV;
import static no.nav.safselvbetjening.hentdokument.HentDokumentService.HENTDOKUMENT_TILGANG_FIELDS;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_INSTANCE_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@EmbeddedKafka(
		topics = {"test-ut-topic"},
		bootstrapServersProperty = "spring.kafka.bootstrap-servers",
		partitions = 1
)
public abstract class AbstractHentDokumentItest extends AbstractItest {
	@Value("${safselvbetjening.topics.dokdistdittnav}")
	protected static String UT_TOPIC = "test-ut-topic";

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	protected EmbeddedKafkaBroker kafkaEmbedded;

	protected static Consumer<String, HoveddokumentLest> consumer;

	protected static final String JOURNALPOST_ID = "400000000";
	protected static final String DOKUMENT_ID = "410000000";
	protected static final VariantFormatCode VARIANTFORMAT = ARKIV;
	protected static final String BRUKER_ID = "12345678911";
	protected static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@BeforeEach
	public void setUpClass() {
		// KafkaConsumer for å kunne konsumere meldinger som InngaaendeHendelsePublisher dytter til 'test-ut-topic'
		this.setUpConsumerForTopicUt();
	}

	protected void setUpConsumerForTopicUt() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("itest-group", "true", kafkaEmbedded);
		consumerProps.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put(VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
		consumerProps.put(SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
		consumerProps.put(SPECIFIC_AVRO_READER_CONFIG, "true");
		consumerProps.put(GROUP_INSTANCE_ID_CONFIG, "itest-group-instance");

		consumer = new DefaultKafkaConsumerFactory<String, HoveddokumentLest>(consumerProps).createConsumer();
		consumer.subscribe(singletonList(UT_TOPIC));
	}

	protected List<HoveddokumentLest> getAllCurrentRecordsOnTopicUt() {
		return StreamSupport.stream(KafkaTestUtils.getRecords(consumer, ofMillis(500)).records(UT_TOPIC).spliterator(), false)
				.map(ConsumerRecord::value)
				.collect(Collectors.toList());
	}

	protected static void stubHentDokumentDokarkiv() {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(TEST_FILE_BYTES)));
	}

	protected static void stubHentDokumentDokarkiv(HttpStatus httpStatus) {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(httpStatus.value())));
	}

	protected static void stubDokarkivJournalpost() {
		stubDokarkivJournalpost("1c-hentdokument-ok.json");
	}

	protected static void stubDokarkivJournalpost(String fil) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID + "/dokumentInfoId/" + DOKUMENT_ID + "?fields=" + String.join(",", HENTDOKUMENT_TILGANG_FIELDS))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokarkiv/journalpost/" + fil)));
	}

	protected static void stubDokarkivJournalpost(HttpStatus httpStatus) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID + "/dokumentInfoId/" + DOKUMENT_ID + "?fields=" + String.join(",", HENTDOKUMENT_TILGANG_FIELDS))
				.willReturn(aResponse()
						.withStatus(httpStatus.value())));
	}

	protected void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(APPLICATION_PDF);
		assertThat(responseEntity.getHeaders().getContentDisposition().getType()).isEqualTo("inline");
		assertThat(responseEntity.getBody()).isEqualTo(new String(TEST_FILE_BYTES));
		assertThat(responseEntity.getHeaders().getContentDisposition().getFilename()).isEqualTo(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf");
	}

	protected ResponseEntity<String> callHentDokument() {
		return callHentDokument(BRUKER_ID);
	}

	protected ResponseEntity<String> callHentDokumentAsFullmektig() {
		return callHentDokument(FULLMEKTIG_ID);
	}

	protected ResponseEntity<String> callHentDokument(String innloggetBrukerId) {
		String uri = createHentDokumentUri(JOURNALPOST_ID, DOKUMENT_ID, VARIANTFORMAT.name());
		return this.restTemplate.exchange(uri, GET, createHttpEntityHeaders(innloggetBrukerId), String.class);
	}

	protected ResponseEntity<String> callHentDokumentSubToken() {
		String uri = createHentDokumentUri(JOURNALPOST_ID, DOKUMENT_ID, VARIANTFORMAT.name());
		return this.restTemplate.exchange(uri, GET, createHttpEntityHeadersSubToken(BRUKER_ID), String.class);
	}

	protected String createHentDokumentUri(String journalpostId, String dokumentInfoId, String variantFormat) {
		return "/rest/hentdokument/%s/%s/%s".formatted(journalpostId, dokumentInfoId, variantFormat);
	}
}
