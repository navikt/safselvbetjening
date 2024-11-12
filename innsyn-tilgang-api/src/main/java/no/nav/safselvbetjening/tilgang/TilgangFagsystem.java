package no.nav.safselvbetjening.tilgang;

/**
 * TilgangFagsystem er et enum som inneholder alle fagsystemene som er gyldige i tilgangsdomenet. Bruk metoden from(String) for å mappe fra verdiene fra Dokarkiv
 */
public enum TilgangFagsystem {
	GOSYS, PENSJON;

	/**
	 * Map til TilgangFagsystem fra String som representerer fagsystem i joark (dokarkiv). Kun PEN og FS22 støttes nå.
	 *
	 * @param fagsystem Verdi for fagsystem fra dokarkiv sitt api
	 * @return TilgangFagsystem som korresponderer med fagsystemverdi fra dokarkiv
	 * @throws IllegalArgumentException hvis verdien for fagsystem ikke støttes eller er null
	 */
	public static TilgangFagsystem from(String fagsystem) {
		if ("PEN".equals(fagsystem)) {
			return PENSJON;
		} else if ("FS22".equals(fagsystem)) {
			return GOSYS;
		} else {
			throw new IllegalArgumentException("Fagsystem " + fagsystem + " is not supported");
		}
	}
}
