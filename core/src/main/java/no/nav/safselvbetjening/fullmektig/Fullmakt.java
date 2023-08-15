package no.nav.safselvbetjening.fullmektig;

import java.util.List;

public record Fullmakt(String fullmektig, String fullmaktsgiver, List<String> tema) {
	public Fullmakt {
		if(tema == null || tema.isEmpty()) {
			throw new IllegalArgumentException("tema kan ikke være null eller tom");
		}
	}

	public boolean gjelderForTema(String tema) {
		return this.tema.contains(tema);
	}
}
