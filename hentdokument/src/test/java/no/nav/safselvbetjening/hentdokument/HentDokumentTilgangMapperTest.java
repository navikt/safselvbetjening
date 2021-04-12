package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import org.junit.jupiter.api.Test;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode.PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.safselvbetjening.domain.Kanal.LOKAL_UTSKRIFT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_IM;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.DATO_OPPRETTET;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.FAGSYSTEM;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.IDENT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.JOURNALPOST_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.TEMA;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createBaseTilgangJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createTilgangJournalpostDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HentDokumentTilgangMapperTest {

	private final HentDokumentTilgangMapper mapper;

	public HentDokumentTilgangMapperTest() {
		mapper = new HentDokumentTilgangMapper();
	}

	@Test
	void shouldMapTilgangJournalpostDto() {
		Journalpost journalpost = mapper.map(createTilgangJournalpostDto().build());

		assertEquals(JOURNALPOST_ID, journalpost.getJournalpostId());
		assertEquals(AVSENDER_MOTTAKER_ID, journalpost.getAvsenderMottaker().getId());
		assertEquals(I, journalpost.getJournalposttype());
		assertEquals(MOTTATT, journalpost.getJournalstatus());
		assertEquals(NAV_NO, journalpost.getKanal());

		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertEquals(DATO_OPPRETTET, tilgang.getDatoOpprettet());
		assertEquals(DATO_JOURNALFOERT, tilgang.getJournalfoertDato());
		assertEquals(PEN.toString(), tilgang.getFagomradeCode());
		assertEquals(SkjermingType.POL, tilgang.getSkjerming());
		assertEquals(IDENT, tilgang.getTilgangBruker().getBrukerId());

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertEquals(AKTOER_ID, tilgangSak.getAktoerId());
		assertEquals(FAGSYSTEM, tilgangSak.getFagsystem());
		assertEquals(TEMA, tilgangSak.getTema());
		assertTrue(tilgangSak.isFeilregistrert());

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertEquals(SkjermingType.FEIL, dokumentInfo.getDokumentvarianter().get(0).getTilgangVariant().getSkjerming());

		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertEquals(FORVALTNINGSNOTAT.toString(), tilgangDokument.getKategori());
		assertTrue(tilgangDokument.isInnskrenketPartsinnsyn());
		assertTrue(tilgangDokument.isInnskrenketTredjepart());
		assertFalse(tilgangDokument.isOrganinternt());
		assertFalse(tilgangDokument.isKassert());
	}

	@Test
	void shouldMapJournalpostWithoutSakAndBrukerMinimalInput() {
		TilgangJournalpostDto tilgangJournalpostDto = createBaseTilgangJournalpost().build();
		Journalpost journalpost = mapper.map(tilgangJournalpostDto);

		assertNull(journalpost.getTilgang().getTilgangSak());
		assertNull(journalpost.getTilgang().getTilgangBruker());
	}

	@Test
	void shouldMapJournalposttypeIWithoutMottakskanal() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.I)
				.build());

		assertEquals(Kanal.UKJENT, journalpost.getKanal());
	}

	@Test
	void shouldMapJournalposttypeUWithoutMottakskanal() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(FL)
				.build());

		assertEquals(LOKAL_UTSKRIFT, journalpost.getKanal());
	}

	@Test
	void shouldMapJournalposttypeU() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.U)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.build());

		assertEquals(SKAN_IM, journalpost.getKanal());
	}

	@Test
	void shouldMapJournalposttypeN() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.N)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.build());

		assertEquals(INGEN_DISTRIBUSJON, journalpost.getKanal());
	}
}