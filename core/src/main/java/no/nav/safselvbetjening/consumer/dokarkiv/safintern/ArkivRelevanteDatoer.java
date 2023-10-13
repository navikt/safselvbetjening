package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import java.time.OffsetDateTime;

public record ArkivRelevanteDatoer(OffsetDateTime opprettet,
								   OffsetDateTime journalfoert) {
}
