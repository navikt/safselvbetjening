package no.nav.safselvbetjening.tilgang;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.safselvbetjening.tilgang.TilgangFagsystem.GOSYS;
import static no.nav.safselvbetjening.tilgang.TilgangFagsystem.PENSJON;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.tilgang.TilgangJournalposttype.ANNEN;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.FERDIGSTILT;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.MOTTATT;

public class UtledTilgangTestObjects {

	static final String IDENT = "12345678911";
	static final String ANNEN_PART = "23456789101";
	static final String AKTOER_ID = "10000000000";
	static final String ANNEN_AKTOER_ID = "12000000000";
	public static final String TEMA_PENSJON = "PEN";
	public static final String TEMA_DAGPENGER = "DAG";
	public static final String TEMA_FARSKAP = "FAR";
	public static final String TEMA_KONTROLL = "KTR";
	public static final String TEMA_KONTROLL_ANMELDELSE = "KTA";
	public static final String TEMA_ARBEIDSRAADGIVNING_SKJERMET = "ARS";
	public static final String TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER = "ARP";
	static final TilgangFagsystem ARKIVSAKSYSTEM_GOSYS = GOSYS;
	static final TilgangFagsystem ARKIVSAKSYSTEM_PENSJON = PENSJON;

	static TilgangJournalpost.TilgangJournalpostBuilder baseTilgangJournalpost(String tema, TilgangInnsyn innsyn) {
		return TilgangJournalpost.builder()
				.tilgangBruker(new TilgangBruker(Foedselsnummer.of(IDENT)))
				.datoOpprettet(LocalDateTime.now())
				.tema(tema)
				.journalfoertDato(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING)
				.innsyn(innsyn)
				.tilgangSak(TilgangGosysSak.builder()
						.aktoerId(AktoerId.of(AKTOER_ID))
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(false)
						.tema(tema)
						.build());
	}

	static TilgangJournalpost.TilgangJournalpostBuilder baseJournalfoertJournalpost(String tema, TilgangInnsyn innsyn) {
		return baseTilgangJournalpost(tema, innsyn)
				.journalpostId(40000000)
				.journalstatus(FERDIGSTILT)
				.journalposttype(ANNEN)
				.avsenderMottakerId(IDENT)
				.skjerming(TilgangSkjermingType.INGEN_SKJERMING)
				.dokumenter(List.of(tilgangDokument()));
	}

	static TilgangDokument.TilgangDokumentBuilder baseTilgangDokument() {
		return TilgangDokument.builder()
				.kassert(false)
				.skjerming(TilgangSkjermingType.INGEN_SKJERMING)
				.kategori("ES")
				.dokumentvarianter(List.of(tilgangVariant()));
	}

	static TilgangDokument tilgangDokument() {
		return baseTilgangDokument().build();
	}

	static TilgangVariant.TilgangVariantBuilder baseTilgangVariant() {
		return TilgangVariant.builder()
				.skjerming(TilgangSkjermingType.INGEN_SKJERMING)
				.variantformat(TilgangVariantFormat.ARKIV);
	}
	static TilgangVariant tilgangVariant() {
		return baseTilgangVariant().build();
	}

	static TilgangJournalpost.TilgangJournalpostBuilder baseMottattJournalpost() {
		return baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).journalstatus(MOTTATT)
				.tilgangBruker(null)
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_DAGPENGER)
				.journalfoertDato(null)
				.tilgangSak(null);
	}

	static List<Ident> defaultBrukerIdenter() {
		return List.of(AktoerId.of(AKTOER_ID), Foedselsnummer.of(IDENT));
	}
}
