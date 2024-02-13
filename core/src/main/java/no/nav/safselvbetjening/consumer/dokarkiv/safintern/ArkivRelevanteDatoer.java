package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import java.time.OffsetDateTime;

public record ArkivRelevanteDatoer(OffsetDateTime opprettet,
								   OffsetDateTime journalfoert,
								   // kun metadata, ikke brukt til tilgangskontroll
								   OffsetDateTime ekspedert,
								   OffsetDateTime forsendelseMottatt,
								   OffsetDateTime hoveddokument,
								   OffsetDateTime lest,
								   OffsetDateTime retur,
								   OffsetDateTime sendtPrint
) {
}
