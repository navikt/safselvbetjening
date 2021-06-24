package no.nav.safselvbetjening.dokumentoversikt;

import lombok.Value;
import no.nav.safselvbetjening.domain.Journalpost;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder journalpost svar fra joark med litt stats.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
class Journalpostdata {
	private final int antallFoerFiltrering;
	private final List<Journalpost> journalposter;

	int getAntallEtterFiltrering() {
		return journalposter.size();
	}

	static Journalpostdata empty() {
		return new Journalpostdata(0, new ArrayList<>());
	}
}


