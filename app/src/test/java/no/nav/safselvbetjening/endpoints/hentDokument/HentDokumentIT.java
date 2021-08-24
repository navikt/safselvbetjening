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
import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.SKANNET_DOKUMENT;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
		stubFor(get("/fagarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnBadRequestWhenJournalpostIdNotNumeric() {
		stubAzure();

		String uri = "/rest/hentdokument/123456a/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHttpEntityHeaders(BRUKER_ID), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
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
	void hentDokumentTilgangAvvist() {
		stubPdl();
		stubAzure();

		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/tilgangjournalpost_gdpr.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(GDPR));
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

	@Test
	void shouldReturnUnauthorizedWithNavReasonBrukerMatcherIkkeTokenWhenTokenDoesNotBelongToBruker() {
		stubPdl();
		stubAzure();
		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv();

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHttpEntityHeaders("22222222222"), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(BRUKER_MATCHER_IKKE_TOKEN));
	}

	@Test
	void shouldReturnUnauthorizedWhenLokalprintSkannet() {
		stubPdl();
		stubAzure();
		stubHentTilgangJournalpostDokarkiv("tilgangjournalpost_lokalprint_skannet.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(SKANNET_DOKUMENT));
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

	private void stubHentTilgangJournalpostDokarkiv(final String file) {
		stubFor(get("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fagarkiv/" + file)));
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

	private ResponseEntity<String> callHentDokumentSubToken() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntityHeadersSubToken(BRUKER_ID), String.class);
	}

}
