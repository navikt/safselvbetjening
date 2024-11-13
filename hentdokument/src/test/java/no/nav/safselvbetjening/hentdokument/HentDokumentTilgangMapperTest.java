package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangFagsystem;
import no.nav.safselvbetjening.tilgang.TilgangGosysSak;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangMottakskanal;
import no.nav.safselvbetjening.tilgang.TilgangPensjonSak;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.FagomradeCode.PEN;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode.SLADDET;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Journalstatus.EKSPEDERT;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.DATO_OPPRETTET;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.IDENT;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.JOURNALPOST_ID;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.TEMA;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.TEMA_PENSJON_UFO;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.arkivJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.baseArkivJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createBrukerIdenter;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.pensjonArkivJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.utgaaendeArkivJournalpost;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.BRUK_STANDARDREGLER;
import static org.assertj.core.api.Assertions.assertThat;

class HentDokumentTilgangMapperTest {

	private static final Ident FOEDSELSNUMMER = Ident.of(IDENT);
	private static final String ARKIV_VARIANT = VariantFormatCode.ARKIV.name();
	private final HentDokumentTilgangMapper mapper = new HentDokumentTilgangMapper();

	@Test
	void shouldMapInngaaendeJournalpostFromArkivJournalpostWhenGsak() {
		ArkivJournalpost arkivJournalpost = arkivJournalpost();
		Journalpost journalpost = mapper.map(arkivJournalpost, arkivJournalpost.dokumenter().getFirst().dokumentInfoId(), ARKIV_VARIANT, createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);

		TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(TilgangJournalstatus.MOTTATT);
		assertThat(tilgang.getTema()).isEqualTo(PEN.name());
		assertThat(tilgang.getSkjerming().erSkjermet).isTrue();
		assertThat(tilgang.getMottakskanal()).isEqualTo(TilgangMottakskanal.IKKE_SKANNING);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(Ident.of(AVSENDER_MOTTAKER_ID));
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(DATO_JOURNALFOERT.toLocalDateTime());

		TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.brukerId()).isEqualTo(FOEDSELSNUMMER);

		assertThat(tilgang.getTilgangSak()).isInstanceOf(TilgangGosysSak.class);
		TilgangGosysSak tilgangSak = (TilgangGosysSak) tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isEqualTo(Ident.of(AKTOER_ID));
		assertThat(tilgangSak.getFagsystem()).isEqualTo(TilgangFagsystem.GOSYS);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();

		DokumentInfo dokumentInfo = journalpost.getDokumenter().getFirst();
		assertThat(dokumentInfo.isHoveddokument()).isTrue();
		TilgangDokument tilgangDokument = tilgang.getDokumenter().getFirst();
		assertThat(tilgangDokument.kategori()).isEqualTo(FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.kassert()).isFalse();

		TilgangVariant tilgangVariant = tilgangDokument.dokumentvarianter().getFirst();
		assertThat(tilgangVariant.skjerming().erSkjermet).isTrue();
	}

	@Test
	void shouldMapUtgaaendeJournalpostFromArkivJournalpostWhenGsak() {
		ArkivJournalpost arkivJournalpost = utgaaendeArkivJournalpost();
		Journalpost journalpost = mapper.map(arkivJournalpost, arkivJournalpost.dokumenter().getFirst().dokumentInfoId(), ARKIV_VARIANT, createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);

		TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(TilgangJournalstatus.FERDIGSTILT);
		assertThat(tilgang.getTema()).isEqualTo(PEN.name());
		assertThat(tilgang.getSkjerming().erSkjermet).isTrue();
		assertThat(tilgang.getMottakskanal()).isEqualTo(TilgangMottakskanal.IKKE_SKANNING);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(Ident.of(AVSENDER_MOTTAKER_ID));
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(DATO_JOURNALFOERT.toLocalDateTime());

		TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.brukerId()).isEqualTo(FOEDSELSNUMMER);

		assertThat(tilgang.getTilgangSak()).isInstanceOf(TilgangGosysSak.class);
		TilgangGosysSak tilgangSak = (TilgangGosysSak) tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isEqualTo(Ident.of(AKTOER_ID));
		assertThat(tilgangSak.getFagsystem()).isEqualTo(TilgangFagsystem.GOSYS);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();

		DokumentInfo dokumentInfo = journalpost.getDokumenter().getFirst();
		assertThat(dokumentInfo.isHoveddokument()).isTrue();
		TilgangDokument tilgangDokument = tilgang.getDokumenter().getFirst();
		assertThat(tilgangDokument.kategori()).isEqualTo(FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.kassert()).isFalse();

		TilgangVariant tilgangVariant = tilgangDokument.dokumentvarianter().getFirst();
		assertThat(tilgangVariant.skjerming().erSkjermet).isTrue();
	}

	@Test
	void shouldMapJournalpostFromArkivJournalpostWhenPensjonssak() {
		ArkivJournalpost arkivJournalpost = pensjonArkivJournalpost();
		Pensjonsak pensjonsak = new Pensjonsak(123L, TEMA_PENSJON_UFO);
		Journalpost journalpost = mapper.map(arkivJournalpost, arkivJournalpost.dokumenter().getFirst().dokumentInfoId(), ARKIV_VARIANT, createBrukerIdenter(), Optional.of(pensjonsak));
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);

		TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(TilgangJournalstatus.MOTTATT);
		assertThat(tilgang.getTema()).isEqualTo(PEN.name());
		assertThat(tilgang.getSkjerming().erSkjermet).isTrue();
		assertThat(tilgang.getMottakskanal()).isEqualTo(TilgangMottakskanal.IKKE_SKANNING);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(Ident.of(AVSENDER_MOTTAKER_ID));
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(DATO_JOURNALFOERT.toLocalDateTime());

		TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.brukerId()).isEqualTo(FOEDSELSNUMMER);

		assertThat(tilgang.getTilgangSak()).isInstanceOf(TilgangPensjonSak.class);
		TilgangPensjonSak tilgangSak = (TilgangPensjonSak) tilgang.getTilgangSak();
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(FOEDSELSNUMMER);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(TilgangFagsystem.PENSJON);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA_PENSJON_UFO);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();

		DokumentInfo dokumentInfo = journalpost.getDokumenter().getFirst();
		assertThat(dokumentInfo.isHoveddokument()).isTrue();
		TilgangDokument tilgangDokument = tilgang.getDokumenter().getFirst();
		assertThat(tilgangDokument.kategori()).isEqualTo(FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.kassert()).isFalse();

		TilgangVariant tilgangVariant = tilgangDokument.dokumentvarianter().getFirst();
		assertThat(tilgangVariant.skjerming().erSkjermet).isTrue();
	}

	@Test
	void shouldMapEmptyDokumentvariantWhenNoVariantFound() {
		ArkivJournalpost arkivJournalpost = arkivJournalpost();
		Journalpost journalpost = mapper.map(arkivJournalpost, arkivJournalpost.dokumenter().getFirst().dokumentInfoId(), SLADDET.name(), createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter()).hasSize(0);
	}

	@Test
	void shouldMapTilgangJournalpostDtoWhenPensjonSakTemaNull() {
		ArkivJournalpost arkivJournalpost = pensjonArkivJournalpost();
		Pensjonsak pensjonsak = new Pensjonsak(123L, null);
		Journalpost journalpost = mapper.map(arkivJournalpost, arkivJournalpost.dokumenter().getFirst().dokumentInfoId(), ARKIV_VARIANT, createBrukerIdenter(), Optional.of(pensjonsak));

		assertThat(journalpost.getTilgang().getTilgangSak().getTema()).isEqualTo(PEN.name());
	}

	@Test
	void shouldMapJournalpostWithoutSakAndBrukerMinimalInput() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost().build();
		Journalpost journalpost = mapper.map(arkivJournalpost, 0, ARKIV_VARIANT, createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getTilgang().getTilgangSak()).isNull();
		assertThat(journalpost.getTilgang().getTilgangBruker()).isNull();
	}
}