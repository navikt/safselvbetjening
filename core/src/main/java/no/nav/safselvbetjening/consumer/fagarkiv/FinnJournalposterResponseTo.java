package no.nav.safselvbetjening.consumer.fagarkiv;

import lombok.Data;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class FinnJournalposterResponseTo {
	private List<JournalpostDto> tilgangJournalposter;
}
