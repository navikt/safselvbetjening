package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;
import lombok.ToString;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;

import java.util.List;

@Builder
public record FinnJournalposterRequest(
		List<Long> gsakSakIds,
		List<Long> psakSakIds,
		String fraDato,
		String tilDato,
		Boolean visFeilregistrerte,
		@ToString.Exclude
		List<String> alleIdenter,
		List<JournalStatusCode> journalstatuser,
		List<JournalpostTypeCode> journalposttyper,
		Integer antallRader,
		String etterPeker
) {
}
