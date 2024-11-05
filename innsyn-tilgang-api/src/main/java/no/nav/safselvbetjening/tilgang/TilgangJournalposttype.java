package no.nav.safselvbetjening.tilgang;

public enum TilgangJournalposttype {
	ANNEN, NOTAT;

	public static TilgangJournalposttype from(String value) {
		if ("N".equals(value)) {
			return NOTAT;
		}
		return ANNEN;
	}
}
