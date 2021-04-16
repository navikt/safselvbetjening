package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

class HentDokumentIT extends AbstractItest {

	private static final String DOKUMENT_ID = "123";
	private static final String JOURNALPOST_ID = "123";
	private static final String BRUKER_ID = "12345678911";
	private static final VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static final VariantFormatCode SLADDET_VARIANTFORMAT = VariantFormatCode.SLADDET;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@Test
	void happyPath() {

		stubHentDokumentDokarkiv();
		stubHentTilgangJournalpostDokarkiv("/fagarkiv/tilgangJournalpostResponse.json");

		/*given()
				.header("Authorization", "Bearer " + token1)
				.when()
				.get(uri)
				.then()
				.log().ifValidationFails()
				.statusCode(HttpStatus.OK.value());*/

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);

	}

	private void stubHentDokumentDokarkiv() {
		stubFor(get("/fagarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));
	}

	private void stubHentTilgangJournalpostDokarkiv(String fil) {
		stubFor(get(urlEqualTo("/fagarkiv/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile(fil)));
	}

	private void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private ResponseEntity<String> callHentDokument() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntityHeaders(BRUKER_ID), String.class);
	}

}
