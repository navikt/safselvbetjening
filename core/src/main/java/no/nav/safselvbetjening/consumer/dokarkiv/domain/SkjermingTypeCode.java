package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import java.util.EnumSet;

/**
 * Enum for codes in T_K_BEGRENSNING_TYPE.
 */
public enum SkjermingTypeCode {
	POL,
	FEIL;

	private static final EnumSet<SkjermingTypeCode> VALUES = EnumSet.of(POL, FEIL);

	public static EnumSet<SkjermingTypeCode> asList() {
		return VALUES;
	}
}
