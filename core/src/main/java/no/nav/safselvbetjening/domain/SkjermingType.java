package no.nav.safselvbetjening.domain;

import java.util.EnumSet;

public enum SkjermingType {
	POL,
	FEIL;

	public static EnumSet<SkjermingType> asList() {
		return EnumSet.of(POL, FEIL);
	}
}
