package no.nav.safselvbetjening.dokumentoversikt;

import lombok.Value;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.Saker;

@Value
public class Basedata {
	BrukerIdenter brukerIdenter;
	Saker saker;
}
