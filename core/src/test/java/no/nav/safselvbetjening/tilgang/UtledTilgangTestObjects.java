package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.pdl.PdlResponse;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Innsyn;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.Variantformat;
import no.nav.safselvbetjening.service.BrukerIdenter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static no.nav.safselvbetjening.domain.Tema.DAG;
import static no.nav.safselvbetjening.domain.Tema.PEN;

public class UtledTilgangTestObjects {

	static final String IDENT = "12345678911";
	static final String ANNEN_PART = "23456789101";
	static final String AKTOER_ID = "10000000000";
	static final String ANNEN_AKTOER_ID = "12000000000";
	public static final String TEMA_PENSJON = PEN.name();
	public static final String TEMA_DAGPENGER = DAG.name();
	public static final String TEMA_FARSKAP = "FAR";
	public static final String TEMA_KONTROLL = "KTR";
	public static final String TEMA_KONTROLL_ANMELDELSE = "KTA";
	public static final String TEMA_ARBEIDSRAADGIVNING_SKJERMET = "ARS";
	public static final String TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER = "ARP";
	static final String ARKIVSAKSYSTEM_GOSYS = "FS22";
	public static final String ARKIVSAKSYSTEM_PENSJON = "PEN";
	static final LocalDateTime FOER_INNSYNSDATO = LocalDateTime.parse("2016-01-01T12:00");

	static Journalpost.JournalpostBuilder baseJournalpost(String tema, Innsyn innsyn) {
		return Journalpost.builder()
				.journalpostId("40000000")
				.tittel("Søknad om dagpenger")
				.journalstatus(Journalstatus.JOURNALFOERT)
				.kanal(Kanal.NAV_NO)
				.journalposttype(Journalposttype.I)
				.avsender(AvsenderMottaker.builder()
						.id(IDENT)
						.type(AvsenderMottakerIdType.FNR)
						.build())
				.relevanteDatoer(List.of(new RelevantDato(new Date(), Datotype.DATO_JOURNALFOERT)))
				.tilgang(baseTilgangJournalpost(tema, innsyn).build())
				.dokumenter(List.of(DokumentInfo.builder()
						.dokumentInfoId("40000000")
						.tittel("Søknad om dagpenger")
						.brevkode("Dagpenger-01")
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.kassert(false)
								.kategori("ES")
								.build())
						.dokumentvarianter(List.of(Dokumentvariant.builder()
								.filuuid("aaa-bbb-ccc-ddd")
								.variantformat(Variantformat.ARKIV)
								.tilgangVariant(Dokumentvariant.TilgangVariant.builder()
										.build())
								.build()))
						.build()));
	}

	static Journalpost.TilgangJournalpost.TilgangJournalpostBuilder baseTilgangJournalpost(String tema, Innsyn innsyn) {
		return Journalpost.TilgangJournalpost.builder()
				.tilgangBruker(Journalpost.TilgangBruker.builder()
						.brukerId(IDENT)
						.build())
				.datoOpprettet(LocalDateTime.now())
				.tema(tema)
				.journalfoertDato(LocalDateTime.now())
				.innsyn(innsyn)
				.tilgangSak(Journalpost.TilgangSak.builder()
						.aktoerId(AKTOER_ID)
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(false)
						.tema(tema)
						.build());
	}

	static Journalpost.JournalpostBuilder baseJournalfoertJournalpost(String tema, Innsyn innsyn) {
		return baseJournalpost(tema, innsyn).journalstatus(Journalstatus.JOURNALFOERT);
	}

	static Journalpost.JournalpostBuilder baseMottattJournalpost() {
		return baseJournalpost(TEMA_DAGPENGER, null).journalstatus(Journalstatus.MOTTATT)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangBruker(null)
						.datoOpprettet(LocalDateTime.now())
						.tema(TEMA_DAGPENGER)
						.journalfoertDato(null)
						.tilgangSak(null)
						.build());
	}

	static BrukerIdenter defaultBrukerIdenter() {
		PdlResponse.PdlIdent fnr = new PdlResponse.PdlIdent();
		fnr.setGruppe(PdlResponse.PdlGruppe.FOLKEREGISTERIDENT);
		fnr.setHistorisk(false);
		fnr.setIdent(IDENT);
		PdlResponse.PdlIdent aktoerId = new PdlResponse.PdlIdent();
		aktoerId.setGruppe(PdlResponse.PdlGruppe.AKTORID);
		aktoerId.setHistorisk(false);
		aktoerId.setIdent(AKTOER_ID);
		return new BrukerIdenter(Arrays.asList(fnr, aktoerId));
	}
}
