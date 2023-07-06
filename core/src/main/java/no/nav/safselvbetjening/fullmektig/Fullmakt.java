package no.nav.safselvbetjening.fullmektig;

import java.util.List;

public record Fullmakt(List<String> tema) {
	public Fullmakt {
		if(tema == null || tema.isEmpty()) {
			throw new IllegalArgumentException("tema kan ikke være null eller tom");
		}
	}
}
