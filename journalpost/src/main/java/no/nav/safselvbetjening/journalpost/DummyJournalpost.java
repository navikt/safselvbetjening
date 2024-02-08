package no.nav.safselvbetjening.journalpost;

import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.Sak;
import no.nav.safselvbetjening.domain.Sakstype;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.domain.Variantformat;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class DummyJournalpost {
	static Journalpost stub(String journalpostId) {
		return Journalpost.builder()
				.journalpostId(journalpostId)
				.journalposttype(Journalposttype.I)
				.tema(Tema.AAP.name())
				.journalstatus(Journalstatus.JOURNALFOERT)
				.sak(Sak.builder()
						.fagsakId("10000000")
						.fagsaksystem("AO01")
						.sakstype(Sakstype.FAGSAK)
						.build())
				.relevanteDatoer(List.of(
						new RelevantDato(Date.from(OffsetDateTime.parse("2024-02-07T08:00:00.000+00:00").toInstant()), Datotype.DATO_REGISTRERT),
						new RelevantDato(Date.from(OffsetDateTime.parse("2024-02-07T09:00:00.000+00:00").toInstant()), Datotype.DATO_JOURNALFOERT),
						new RelevantDato(Date.from(OffsetDateTime.parse("2024-02-07T09:00:00.000+00:00").toInstant()), Datotype.DATO_DOKUMENT),
						new RelevantDato(Date.from(OffsetDateTime.parse("2024-02-07T09:00:00.000+00:00").toInstant()), Datotype.DATO_JOURNALFOERT)
				))
				.avsender(AvsenderMottaker.builder()
						// Test-norge ident HELLIG BOMULL dolly bestilling 94222
						.id("16848099787")
						.type(AvsenderMottakerIdType.FNR)
						.build())
				.tittel("Søknad om arbeidsavklaringspenger")
				.eksternReferanseId(UUID.randomUUID().toString())
				.kanal(Kanal.NAV_NO)
				.dokumenter(List.of(
						DokumentInfo.builder()
								.dokumentInfoId("100000000")
								.tittel("Søknad om arbeidsavklaringspenger")
								.brevkode("NAV 11-13.05")
								.hoveddokument(true)
								.dokumentvarianter(List.of(
										Dokumentvariant.builder()
												.filuuid(UUID.randomUUID().toString())
												.filtype("PDF")
												.filstorrelse(1000)
												.variantformat(Variantformat.ARKIV)
												.brukerHarTilgang(false)
												.build()
								))
								.build()
				))
				.build();
	}
}
