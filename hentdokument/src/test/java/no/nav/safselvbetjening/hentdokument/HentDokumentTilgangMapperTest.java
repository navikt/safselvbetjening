package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode.PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.domain.Innsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.safselvbetjening.domain.Kanal.LOKAL_UTSKRIFT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_IM;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.ARKIVSAKSYSTEM_GOSYS;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.ARKIVSAKSYSTEM_PENSJON;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.DATO_OPPRETTET;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.IDENT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.JOURNALPOST_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.TEMA;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.TEMA_PENSJON_UFO;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createBaseTilgangJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createBrukerIdenter;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createTilgangJournalpostDto;
import static org.assertj.core.api.Assertions.assertThat;
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
		Journalpost journalpost = mapper.map(createTilgangJournalpostDto().build(), createBrukerIdenter(), Optional.empty());

		assertEquals(JOURNALPOST_ID, journalpost.getJournalpostId());
		assertEquals(AVSENDER_MOTTAKER_ID, journalpost.getTilgang().getAvsenderMottakerId());
		assertEquals(I, journalpost.getJournalposttype());
		assertEquals(MOTTATT, journalpost.getJournalstatus());
		assertEquals(NAV_NO, journalpost.getKanal());

		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(M.name());
		assertEquals(DATO_OPPRETTET, tilgang.getDatoOpprettet());
		assertEquals(DATO_JOURNALFOERT, tilgang.getJournalfoertDato());
		assertEquals(PEN.toString(), tilgang.getTema());
		assertEquals(SkjermingType.POL, tilgang.getSkjerming());
		assertEquals(IDENT, tilgang.getTilgangBruker().getBrukerId());
		assertEquals(NAV_NO, tilgang.getMottakskanal());

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertEquals(AKTOER_ID, tilgangSak.getAktoerId());
		assertThat(tilgangSak.getFoedselsnummer()).isNull();
		assertEquals(ARKIVSAKSYSTEM_GOSYS, tilgangSak.getFagsystem());
		assertEquals(TEMA, tilgangSak.getTema());
		assertTrue(tilgangSak.isFeilregistrert());
		assertEquals(BRUK_STANDARDREGLER, tilgang.getInnsyn());

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertEquals(SkjermingType.FEIL, dokumentInfo.getDokumentvarianter().get(0).getTilgangVariant().getSkjerming());

		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertEquals(FORVALTNINGSNOTAT, tilgangDokument.getKategori());
		assertFalse(tilgangDokument.isKassert());
	}

	@Test
	void shouldMapTilgangJournalpostDtoWhenPensjonSak() {
		TilgangJournalpostDto build = createTilgangJournalpostDto().build();
		build.getSak().setFagsystem(ARKIVSAKSYSTEM_PENSJON);
		Pensjonsak pensjonsak = new Pensjonsak("123", TEMA_PENSJON_UFO);
		Journalpost journalpost = mapper.map(build, createBrukerIdenter(), Optional.of(pensjonsak));

		assertEquals(JOURNALPOST_ID, journalpost.getJournalpostId());
		assertEquals(AVSENDER_MOTTAKER_ID, journalpost.getTilgang().getAvsenderMottakerId());
		assertEquals(I, journalpost.getJournalposttype());
		assertEquals(MOTTATT, journalpost.getJournalstatus());
		assertEquals(NAV_NO, journalpost.getKanal());

		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(M.name());
		assertEquals(DATO_OPPRETTET, tilgang.getDatoOpprettet());
		assertEquals(DATO_JOURNALFOERT, tilgang.getJournalfoertDato());
		assertEquals(PEN.name(), tilgang.getTema());
		assertEquals(SkjermingType.POL, tilgang.getSkjerming());
		assertEquals(IDENT, tilgang.getTilgangBruker().getBrukerId());

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertEquals(AKTOER_ID, tilgangSak.getAktoerId());
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(IDENT);
		assertEquals(ARKIVSAKSYSTEM_PENSJON, tilgangSak.getFagsystem());
		assertEquals(TEMA_PENSJON_UFO, tilgangSak.getTema());
		assertTrue(tilgangSak.isFeilregistrert());

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertEquals(SkjermingType.FEIL, dokumentInfo.getDokumentvarianter().get(0).getTilgangVariant().getSkjerming());

		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertEquals(FORVALTNINGSNOTAT, tilgangDokument.getKategori());
		assertFalse(tilgangDokument.isKassert());
	}

	@Test
	void shouldMapJournalpostWithoutSakAndBrukerMinimalInput() {
		TilgangJournalpostDto tilgangJournalpostDto = createBaseTilgangJournalpost().build();
		Journalpost journalpost = mapper.map(tilgangJournalpostDto, createBrukerIdenter(), Optional.empty());

		assertNull(journalpost.getTilgang().getTilgangSak());
		assertNull(journalpost.getTilgang().getTilgangBruker());
	}

	@Test
	void shouldMapJournalposttypeIWithoutMottakskanal() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.I)
				.build(), createBrukerIdenter(), Optional.empty());

		assertEquals(Kanal.UKJENT, journalpost.getKanal());
	}

	@Test
	void shouldMapJournalposttypeUWithoutMottakskanal() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(FL)
				.build(), createBrukerIdenter(), Optional.empty());

		assertEquals(LOKAL_UTSKRIFT, journalpost.getKanal());
	}

	@Test
	void shouldMapJournalposttypeU() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.U)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.build(), createBrukerIdenter(), Optional.empty());

		assertEquals(SKAN_IM, journalpost.getKanal());
	}

	@Test
	void shouldMapJournalposttypeN() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.N)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.build(), createBrukerIdenter(), Optional.empty());

		assertEquals(INGEN_DISTRIBUSJON, journalpost.getKanal());
	}

	@Test
	void shouldMapTilgangJournalpostWhenLokalUtskriftSkannet() {
		Journalpost journalpost = mapper.map(TilgangJournalpostDto.builder()
				.journalpostType(JournalpostTypeCode.U)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.build(), createBrukerIdenter(), Optional.empty());

		assertEquals(SKAN_IM, journalpost.getTilgang().getMottakskanal());
	}
}