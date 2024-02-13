package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;

import java.util.List;

@Builder
public record ArkivJournalpost(Long journalpostId,
							   String type,
							   String fagomraade,
							   String status,
							   String mottakskanal,
							   String utsendingskanal,
							   String innsyn,
							   String skjerming,
							   ArkivRelevanteDatoer relevanteDatoer,
							   ArkivAvsenderMottaker avsenderMottaker,
							   ArkivBruker bruker,
							   ArkivSaksrelasjon saksrelasjon,
							   List<ArkivDokumentinfo> dokumenter,
							   // kun metadata, ikke brukt til tilgangskontroll
							   String innhold,
							   String kanalreferanseId

) {
	public boolean isTilknyttetSak() {
		return saksrelasjon != null && saksrelasjon.sakId() != null;
	}
}
