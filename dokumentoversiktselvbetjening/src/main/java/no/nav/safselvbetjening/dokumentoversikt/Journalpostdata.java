package no.nav.safselvbetjening.dokumentoversikt;

import lombok.Value;
import no.nav.safselvbetjening.domain.Journalpost;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder journalpost svar fra joark med litt stats.
 */
@Value
class Journalpostdata {
	int antallFoerFiltrering;
	List<Journalpost> journalposter;

	int getAntallEtterFiltrering() {
		return journalposter.size();
	}

	static Journalpostdata empty() {
		return new Journalpostdata(0, new ArrayList<>());
	}
}


