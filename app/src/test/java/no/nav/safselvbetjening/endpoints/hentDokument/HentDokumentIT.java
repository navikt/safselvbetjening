package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Tester happy paths og noen funksjonelle/teknisk situasjoner
 */
class HentDokumentIT extends AbstractHentDokumentItest {
	/**
	 * Skal hente dokument gitt at alle den passerer alle tilgangsregler
	 */
	@Test
	void shouldHentDokumentWhenHappy() {
		stubPdlGenerell();
		stubAzure();
		stubDokarkivJournalpost();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void shouldHentDokumentWhenBrukerIdentInSubToken() {
		stubPdlGenerell();
		stubAzure();
		stubDokarkivJournalpost();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentSubToken();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentNotFound() {
		stubPdlGenerell();
		stubAzure();
		stubDokarkivJournalpost();
		stubHentDokumentDokarkiv(NOT_FOUND);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Fant ikke dokument med dokumentInfoId=410000000, variantFormat=ARKIV");
	}

	@Test
	void shouldReturnBadRequestWhenJournalpostIdNotNumeric() {
		stubAzure();

		String uri = createHentDokumentUri("123456a", DOKUMENT_ID, VARIANTFORMAT.name());
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, GET, createHttpEntityHeaders(BRUKER_ID), String.class);

		assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("journalpostId er ikke et tall. journalpostId=123456a");
	}

	@Test
	void hentTilgangJournalpostNotFound() {
		stubAzure();
		stubDokarkivJournalpost(NOT_FOUND);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Journalpost med journalpostId=400000000, dokumentInfoId=410000000 ikke funnet i Joark");
	}

	@Test
	void hentDokumentDokarkivTechnicalFail() {
		stubAzure();
		stubDokarkivJournalpost(INTERNAL_SERVER_ERROR);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("hentJournalpost feilet teknisk. status=500 INTERNAL_SERVER_ERROR, journalpostId=400000000, dokumentInfoId=410000000");
	}

	@Test
	void hentDokumentPenHappyPath() {
//		setupKafkaConsumer();
		stubAzure();
		stubPdlGenerell();
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	@Test
	void hentDokumentUtgaaendePenKafkaHappyPath() {
		stubAzure();
		stubPdlGenerell();
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-utgaaende-ok.json");
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(hoveddokumentLest.getDokumentInfoId()).isEqualTo(DOKUMENT_ID);
	}

	@Test
	void hentDokumentPenNotFound() {
		stubAzure();
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-empty.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-utgaaende-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("hentBrukerForSak returnerte tomt fødselsnummer for sakId=2000000");
	}
}
