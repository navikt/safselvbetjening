package no.nav.safselvbetjening.dokumentoversikt;

import lombok.Value;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.Saker;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Basedata {
	private final BrukerIdenter brukerIdenter;
	private final Saker saker;
}
