package no.nav.safselvbetjening.tilgang;

import static org.apache.commons.lang3.StringUtils.isBlank;

public enum TilgangSkjermingType {
	POL(true),
	FEIL(true),
	UKJENT(true),
	INGEN_SKJERMING(false);

	public final boolean erSkjermet;

	TilgangSkjermingType(boolean erSkjermet) {
		this.erSkjermet = erSkjermet;
	}

	public static TilgangSkjermingType from(String value) {
		if (isBlank(value)) {
			return INGEN_SKJERMING;
		}
		return switch (value) {
			case "POL" -> POL;
			case "FEIL" -> FEIL;
			default -> UKJENT;
		};
	}
}
