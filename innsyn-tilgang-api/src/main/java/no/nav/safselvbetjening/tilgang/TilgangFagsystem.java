package no.nav.safselvbetjening.tilgang;

public enum TilgangFagsystem {
	FS22, PEN;

	public static TilgangFagsystem from(String fagsystem) {
		return fagsystem == null ? null : valueOf(fagsystem.toUpperCase());
	}
}
