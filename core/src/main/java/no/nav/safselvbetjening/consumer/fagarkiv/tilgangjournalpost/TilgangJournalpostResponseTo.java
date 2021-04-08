package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TilgangJournalpostResponseTo {
	private TilgangJournalpostDto tilgangJournalpostDto;
}
