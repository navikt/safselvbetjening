package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.SkjermingType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.FagomradeCode.PEN;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode.SLADDET;
import static no.nav.safselvbetjening.domain.Innsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Journalstatus.EKSPEDERT;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.SkjermingType.FEIL;
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
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.arkivJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.baseArkivJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.createBrukerIdenter;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.pensjonArkivJournalpost;
import static no.nav.safselvbetjening.hentdokument.HentDokumentTestObjects.utgaaendeArkivJournalpost;
import static org.assertj.core.api.Assertions.assertThat;

class HentDokumentTilgangMapperTest {

	public static final String ARKIV_VARIANT = VariantFormatCode.ARKIV.name();
	private final HentDokumentTilgangMapper mapper = new HentDokumentTilgangMapper();

	@Test
	void shouldMapInngaaendeJournalpostFromArkivJournalpostWhenGsak() {
		Journalpost journalpost = mapper.map(arkivJournalpost(), ARKIV_VARIANT, createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);

		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(M.name());
		assertThat(tilgang.getTema()).isEqualTo(PEN.name());
		assertThat(tilgang.getSkjerming()).isEqualTo(SkjermingType.POL);
		assertThat(tilgang.getMottakskanal()).isEqualTo(NAV_NO);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(DATO_JOURNALFOERT.toLocalDateTime());

		Journalpost.TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(IDENT);

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(IDENT);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_GOSYS);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.isHoveddokument()).isTrue();
		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertThat(tilgangDokument.getKategori()).isEqualTo(FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.isKassert()).isFalse();

		Dokumentvariant.TilgangVariant tilgangVariant = dokumentInfo.getDokumentvarianter().get(0).getTilgangVariant();
		assertThat(tilgangVariant.getSkjerming()).isEqualTo(FEIL);
	}

	@Test
	void shouldMapUtgaaendeJournalpostFromArkivJournalpostWhenGsak() {
		Journalpost journalpost = mapper.map(utgaaendeArkivJournalpost(), ARKIV_VARIANT, createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(U);
		assertThat(journalpost.getJournalstatus()).isEqualTo(EKSPEDERT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);

		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(E.name());
		assertThat(tilgang.getTema()).isEqualTo(PEN.name());
		assertThat(tilgang.getSkjerming()).isEqualTo(SkjermingType.POL);
		assertThat(tilgang.getMottakskanal()).isNull();
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(DATO_JOURNALFOERT.toLocalDateTime());

		Journalpost.TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(IDENT);

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(IDENT);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_GOSYS);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.isHoveddokument()).isTrue();
		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertThat(tilgangDokument.getKategori()).isEqualTo(FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.isKassert()).isFalse();

		Dokumentvariant.TilgangVariant tilgangVariant = dokumentInfo.getDokumentvarianter().get(0).getTilgangVariant();
		assertThat(tilgangVariant.getSkjerming()).isEqualTo(FEIL);
	}

	@Test
	void shouldMapJournalpostFromArkivJournalpostWhenPensjonssak() {
		ArkivJournalpost arkivJournalpost = pensjonArkivJournalpost();
		Pensjonsak pensjonsak = new Pensjonsak("123", TEMA_PENSJON_UFO);
		Journalpost journalpost = mapper.map(arkivJournalpost, ARKIV_VARIANT, createBrukerIdenter(), Optional.of(pensjonsak));
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);

		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		assertThat(tilgang.getJournalstatus()).isEqualTo(M.name());
		assertThat(tilgang.getTema()).isEqualTo(PEN.name());
		assertThat(tilgang.getSkjerming()).isEqualTo(SkjermingType.POL);
		assertThat(tilgang.getMottakskanal()).isEqualTo(NAV_NO);
		assertThat(tilgang.getAvsenderMottakerId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(tilgang.getInnsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(tilgang.getDatoOpprettet()).isEqualTo(DATO_OPPRETTET.toLocalDateTime());
		assertThat(tilgang.getJournalfoertDato()).isEqualTo(DATO_JOURNALFOERT.toLocalDateTime());

		Journalpost.TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
		assertThat(tilgangBruker.getBrukerId()).isEqualTo(IDENT);

		Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
		assertThat(tilgangSak.getAktoerId()).isNull();
		assertThat(tilgangSak.getFoedselsnummer()).isEqualTo(IDENT);
		assertThat(tilgangSak.getFagsystem()).isEqualTo(ARKIVSAKSYSTEM_PENSJON);
		assertThat(tilgangSak.getTema()).isEqualTo(TEMA_PENSJON_UFO);
		assertThat(tilgangSak.isFeilregistrert()).isTrue();

		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.isHoveddokument()).isTrue();
		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		assertThat(tilgangDokument.getKategori()).isEqualTo(FORVALTNINGSNOTAT);
		assertThat(tilgangDokument.isKassert()).isFalse();

		Dokumentvariant.TilgangVariant tilgangVariant = dokumentInfo.getDokumentvarianter().get(0).getTilgangVariant();
		assertThat(tilgangVariant.getSkjerming()).isEqualTo(FEIL);
	}

	@Test
	void shouldMapEmptyDokumentvariantWhenNoVariantFound() {
		ArkivJournalpost arkivJournalpost = arkivJournalpost();
		Journalpost journalpost = mapper.map(arkivJournalpost, SLADDET.name(), createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getDokumenter().get(0).getDokumentvarianter()).hasSize(0);
	}

	@Test
	void shouldMapTilgangJournalpostDtoWhenPensjonSakTemaNull() {
		ArkivJournalpost arkivJournalpost = pensjonArkivJournalpost();
		Pensjonsak pensjonsak = new Pensjonsak("123", null);
		Journalpost journalpost = mapper.map(arkivJournalpost, ARKIV_VARIANT, createBrukerIdenter(), Optional.of(pensjonsak));

		assertThat(journalpost.getTilgang().getTilgangSak().getTema()).isEqualTo(PEN.name());
	}

	@Test
	void shouldMapJournalpostWithoutSakAndBrukerMinimalInput() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost().build();
		Journalpost journalpost = mapper.map(arkivJournalpost, ARKIV_VARIANT, createBrukerIdenter(), Optional.empty());

		assertThat(journalpost.getTilgang().getTilgangSak()).isNull();
		assertThat(journalpost.getTilgang().getTilgangBruker()).isNull();
	}
}