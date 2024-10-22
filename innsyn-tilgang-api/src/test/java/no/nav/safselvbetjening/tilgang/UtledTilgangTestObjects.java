package no.nav.safselvbetjening.tilgang;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.safselvbetjening.tilgang.TilgangFagsystem.FS22;
import static no.nav.safselvbetjening.tilgang.TilgangFagsystem.PEN;
import static no.nav.safselvbetjening.tilgang.TilgangJournalposttype.I;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.JOURNALFOERT;
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
	static final TilgangFagsystem ARKIVSAKSYSTEM_GOSYS = FS22;
	static final TilgangFagsystem ARKIVSAKSYSTEM_PENSJON = PEN;
	static final LocalDateTime FOER_INNSYNSDATO = LocalDateTime.parse("2016-01-01T12:00");

	static TilgangJournalpost.TilgangJournalpostBuilder baseJournalpost(String tema, TilgangInnsyn innsyn) {
		return baseTilgangJournalpost(tema, innsyn)
				.journalpostId(40000000)
				.journalstatus(JOURNALFOERT)
				.journalposttype(I)
				.avsenderMottakerId(IDENT)
				.skjerming(TilgangSkjermingType.INGEN_SKJERMING)
				.dokumenter(List.of(
						TilgangDokument.builder()
								.kassert(false)
								.skjerming(TilgangSkjermingType.INGEN_SKJERMING)
								.kategori("ES")
								.dokumentvarianter(
										List.of(TilgangVariant.builder()
												.skjerming(TilgangSkjermingType.INGEN_SKJERMING)
												.build()))
								.build()));
	}

	static TilgangJournalpost.TilgangJournalpostBuilder baseTilgangJournalpost(String tema, TilgangInnsyn innsyn) {
		return TilgangJournalpost.builder()
				.tilgangBruker(TilgangBruker.builder()
						.brukerId(IDENT)
						.build())
				.datoOpprettet(LocalDateTime.now())
				.tema(tema)
				.journalfoertDato(LocalDateTime.now())
				.innsyn(innsyn)
				.tilgangSak(TilgangSak.builder()
						.aktoerId(AKTOER_ID)
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(false)
						.tema(tema)
						.build());
	}

	static TilgangJournalpost.TilgangJournalpostBuilder baseJournalfoertJournalpost(String tema, TilgangInnsyn innsyn) {
		return baseJournalpost(tema, innsyn).journalstatus(JOURNALFOERT);
	}

	static TilgangJournalpost.TilgangJournalpostBuilder baseMottattJournalpost() {
		return baseJournalpost(TEMA_DAGPENGER, null).journalstatus(MOTTATT)
				.tilgangBruker(null)
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_DAGPENGER)
				.journalfoertDato(null)
				.tilgangSak(null);
	}

	static List<String> defaultBrukerIdenter() {
		return List.of(AKTOER_ID, IDENT);
	}
}
