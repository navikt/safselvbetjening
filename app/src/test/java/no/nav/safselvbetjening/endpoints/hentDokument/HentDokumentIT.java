package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Tester happy paths og noen funksjonelle/teknisk situasjoner
 */
class HentDokumentIT extends AbstractHentDokumentItest {
	/**
	 * Skal hente dokument gitt at den passerer alle tilgangsregler og ident ligger i pid claim i tokenet
	 */
	@Test
	void skalHenteDokument() {
		stubPdlGenerell();
		stubDokarkivJournalpost();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	/**
	 * Skal hente dokument gitt at den passerer alle tilgangsregler og ident ligger i sub claim i tokenet
	 */
	@Test
	void skalHenteDokumentHvisSubClaim() {
		stubPdlGenerell();
		stubDokarkivJournalpost();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentSubToken();

		assertOkArkivResponse(responseEntity);
	}

	/**
	 * Hvis journalpostId i hentdokument path er ikke-numerisk så skal det returneres en Bad Request feil
	 */
	@Test
	void skalGiBadRequestFeilHvisJournapostIdIkkeNumerisk() {
		String uri = createHentDokumentUri("123456a", DOKUMENT_ID, VARIANTFORMAT.name());
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, GET, createHttpEntityHeaders(BRUKER_ID), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody()).contains("journalpostId er ikke et tall. journalpostId=123456a");
	}

	/**
	 * Hvis dokarkiv hentdokument tjenesten returnerer 404 så skal det returneres Not Found feil
	 */
	@Test
	void skalGiNotFoundFeilHvisDokumentIkkeFinnes() {
		stubPdlGenerell();
		stubDokarkivJournalpost();
		stubHentDokumentDokarkiv(NOT_FOUND);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).contains("Fant ikke dokument med dokumentInfoId=410000000, variantFormat=ARKIV");
	}

	/**
	 * Hvis dokarkiv journalpost metadata tjenesten returnerer 404 så skal det returneres Not Found feil
	 */
	@Test
	void skalGiNotFoundFeilHvisJournalpostIkkeFinnes() {
		stubDokarkivJournalpost(NOT_FOUND);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).contains("Journalpost med journalpostId=400000000, dokumentInfoId=410000000 ikke funnet i Joark");
	}

	/**
	 * Hvis dokarkiv journalpost metadata tjenesten returnerer 5xx så skal det returneres Internal Server Error feil
	 */
	@Test
	void skalGiInternalServerErrorFeilHvisTekniskFeilFraDokarkiv() {
		stubDokarkivJournalpost(INTERNAL_SERVER_ERROR);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).contains("hentJournalpost feilet teknisk. status=500 INTERNAL_SERVER_ERROR, journalpostId=400000000, dokumentInfoId=410000000");
	}

	/**
	 * Hvis dokarkiv journalpost metadata tjenesten returnerer en journalpost som ikke matcher journalpostId det ble spurt på så skal Internal Server Error returneres
	 */
	@Test
	void skalGiInternalServerErrorFeilHvisFeilJournalpost() {
		stubDokarkivJournalpost("1c-hentdokument-feil-journalpost.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).contains("Journalpost som er returnert fra dokarkiv matcher ikke journalpost fra fagarkivet.");
	}

	/**
	 * Hvis journalpost har utsendingskanal NAV_NO og dokumentet som hentes er hoveddokument så skal dokumenteret returneres
	 * og det skal generes en Hoveddokument hendelse for å stoppe revarsling til bruker
	 */
	@Test
	void skalHenteDokumentOgGenerereHoveddokumentLestHendelseHvisUtsendingskanalNavNoOgHoveddokument() {
		stubPdlGenerell();
		stubDokarkivJournalpost("1c-hentdokument-utgaaende-ok.json");
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(hoveddokumentLest.getDokumentInfoId()).isEqualTo(DOKUMENT_ID);
	}

	/**
	 * Hvis journalpost har utsendingskanal NAV_NO og dokumentet som hentes er vedlegg så skal dokumenteret returneres
	 * og det skal ikke generes en Hoveddokument hendelse for å stoppe revarsling til bruker siden dokumentet er et vedlegg
	 */
	@Test
	void skalHenteDokumentOgSkalIkkeGenerereHoveddokumentLestHendelseHvisVedlegg() {
		stubPdlGenerell();
		stubDokarkivJournalpost("1c-hentdokument-utgaaende-vedlegg-ok.json");
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/**
	 * Hvis dokumentet sin journalpost er knyttet til en pensjon sak (fagsystem=PEN) så skal bruker utledes fra pensjon sakId.
	 * Dokumentet returneres hvis bruker på pensjon saken matcher bruker som henter dokumentet
	 */
	@Test
	void skalHenteDokumentHvisDokumentTilknyttetPensjonSak() {
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

	/**
	 * Hvis dokumentet sin journalpost er knyttet til en pensjon sak (fagsystem=PEN) så skal bruker utledes fra pensjon sakId.
	 * Utgående dokument skal generere en HoveddokumentLest kafka hendelse til dokdistdittnav hvis det er et utgående dokument.
	 */
	@Test
	void skalHenteDokumentOgGenerereHoveddokumentLestHendelseHvisUtgaaendeDokumentTilknyttetPensjonSak() {
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

	/**
	 * Hvis dokumentet sin journalpost er knyttet til en pensjon sak (fagsystem=PEN) så skal bruker utledes fra pensjon sakId.
	 * Hvis man ikke finner bruker for pensjon sakId i pensjon så returneres en Not Found feil.
	 */
	@Test
	void skalGiNotFoundFeilHvisBrukerForSakIdIkkeFinnesIPensjon() {
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-empty.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-utgaaende-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(1, getRequestedFor(urlMatching(".*/hentBrukerOgEnhetstilgangerForSak/v1")));
		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).contains("hentBrukerForSak returnerte tomt fødselsnummer for sakId=2000000");
	}
}
