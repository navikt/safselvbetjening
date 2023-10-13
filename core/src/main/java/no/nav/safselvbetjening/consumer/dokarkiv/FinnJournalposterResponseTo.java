package no.nav.safselvbetjening.consumer.dokarkiv;

import lombok.Data;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostDto;

import java.util.List;

@Data
public class FinnJournalposterResponseTo {
	private List<JournalpostDto> tilgangJournalposter;
}
