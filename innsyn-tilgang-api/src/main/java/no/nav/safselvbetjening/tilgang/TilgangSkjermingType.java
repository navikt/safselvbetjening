package no.nav.safselvbetjening.tilgang;

import static no.nav.safselvbetjening.tilgang.UtledTilgangService.isBlank;

/**
 * TilgangSkjermingType er et enum som representerer det subsettet av Skjermingstyper som er relevant for tilgangssjekking.
 * Bruk metoden from(String) for å mappe fra Dokarkiv-verdier
 */
public enum TilgangSkjermingType {
	POL(true),
	FEIL(true),
	UKJENT(true),
	INGEN_SKJERMING(false);

	public final boolean erSkjermet;

	TilgangSkjermingType(boolean erSkjermet) {
		this.erSkjermet = erSkjermet;
	}

	/**
	 * Map til TilgangSkjermingType fra verdi for skjerming fra dokarkiv sitt api
	 *
	 * @param skjermingtype verdi for skjerming fra dokarkiv sitt api
	 * @return TilgangSkjermingType som korresponderer med verdi fra dokarkiv sitt api
	 */
	public static TilgangSkjermingType from(String skjermingtype) {
		if (isBlank(skjermingtype)) {
			return INGEN_SKJERMING;
		}
		return switch (skjermingtype) {
			case "POL" -> POL;
			case "FEIL" -> FEIL;
			default -> UKJENT;
		};
	}
}
