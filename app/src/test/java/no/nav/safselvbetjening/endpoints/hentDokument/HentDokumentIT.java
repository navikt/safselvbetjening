package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

class HentDokumentIT extends AbstractItest {

	private static final String DOKUMENT_ID = "123";
	private static final String JOURNALPOST_ID = "123";
	private static final String BRUKER_ID = "12345678911";
	private static final VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

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
	void hentMidlertidigDokumentHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_midlertidig_happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentNotFound() {
		stubPdl();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentTilgangJournalpostNotFound() {
		stubPdl();
		stubAzure();
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentDokarkivTechnicalFail() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentPdlNotFound() {
		stubAzure();
		stubHentTilgangJournalpostDokarkiv();
		stubHentDokumentDokarkiv();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl_not_found.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentAzureReturnsNotFound() {
		stubHentTilgangJournalpostDokarkiv();
		stubPdl();

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());        //Token returnerer egentlig 404 NOT found. Blir dette riktig håndtering?
	}

	@Test
	void hentDokumentTilgangAvvist() {
		stubPdl();
		stubAzure();

		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_gdpr.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentPenHappyPath() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();

		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_pen_happy.json")));
		stubFor(get("/pensjonsak")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/hentbrukerforsak_happy.json")));
		ResponseEntity<String> responseEntity = callHentDokument();
		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentPenNotFound() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();

		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_pen_happy.json")));
		stubFor(get("/pensjonsak")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/hentbrukerforsak_empty.json")));
		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	private void stubAzure() {
		stubFor(post("/azureTokenUrl")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	private void stubHentDokumentDokarkiv() {
		stubFor(get("/fagarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));
	}

	private void stubHentTilgangJournalpostDokarkiv() {
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_ferdigstilt_happy.json")));
	}

	private void stubPdl() {
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl_happy.json")));
	}

	private void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(MediaType.APPLICATION_PDF, responseEntity.getHeaders().getContentType());
		assertEquals("inline", responseEntity.getHeaders().getContentDisposition().getType());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());
	}

	private ResponseEntity<String> callHentDokument() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntityHeaders(BRUKER_ID), String.class);
	}

}
