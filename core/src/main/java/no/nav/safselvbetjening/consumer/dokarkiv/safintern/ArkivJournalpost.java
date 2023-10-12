package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;

import java.util.List;

@Builder
public record ArkivJournalpost(Long journalpostId,
							   String type,
							   String fagomraade,
							   String status,
							   String mottakskanal,
							   String innsyn,
							   String skjerming,
							   ArkivRelevanteDatoer relevanteDatoer,
							   ArkivAvsenderMottaker avsenderMottaker,
							   ArkivBruker bruker,
							   ArkivSaksrelasjon saksrelasjon,
							   List<ArkivDokumentinfo> dokumenter) {
	public boolean isTilknyttetSak() {
		return saksrelasjon != null && saksrelasjon.sakId() != null;
	}
}
