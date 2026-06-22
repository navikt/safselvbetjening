package no.nav.safselvbetjening.representasjon;

import java.util.Set;

public record Vergemaal(String verge, String vergehaver, Set<String> tema) implements Representasjonsforhold {
	public Vergemaal {
		if(tema == null || tema.isEmpty()) {
			throw new IllegalArgumentException("tema kan ikke være null eller tom");
		}
	}

}
