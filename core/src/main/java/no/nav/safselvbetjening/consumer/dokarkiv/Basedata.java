package no.nav.safselvbetjening.consumer.dokarkiv;

import no.nav.safselvbetjening.service.BrukerIdenter;

public record Basedata(
		BrukerIdenter brukerIdenter,
		Saker saker
) {
}
