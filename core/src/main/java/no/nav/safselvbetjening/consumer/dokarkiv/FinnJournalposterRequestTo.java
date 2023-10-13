package no.nav.safselvbetjening.consumer.dokarkiv;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;

import java.util.List;

@Data
@Builder
public class FinnJournalposterRequestTo {
	private final List<String> gsakSakIds;
	private final List<String> psakSakIds;
	private final String fraDato;
	private final String tilDato;
	private final List<JournalStatusCode> inkluderJournalStatus;
	private final List<JournalpostTypeCode> inkluderJournalpostType;
	private final boolean visFeilregistrerte;
	@ToString.Exclude
	private final List<String> alleIdenter;
	private final Integer foerste;
	private final String etterPeker;
}
