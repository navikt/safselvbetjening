package no.nav.safselvbetjening.representasjon;

import java.util.Set;

public record Fullmakt(String fullmektig, String fullmaktsgiver, Set<String> tema) implements Representasjonsforhold {
	public Fullmakt {
		if(tema == null || tema.isEmpty()) {
			throw new IllegalArgumentException("tema kan ikke være null eller tom");
		}
	}

}
