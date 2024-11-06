package no.nav.safselvbetjening.tilgang;

public enum TilgangFagsystem {
	GOSYS, PENSJON;

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
