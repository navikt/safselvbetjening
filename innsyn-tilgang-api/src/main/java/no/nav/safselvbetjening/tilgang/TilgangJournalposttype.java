package no.nav.safselvbetjening.tilgang;

/**
 * TilgangJournalposttype er et enum som representerer det subsettet av Journalpostttyper som er relevant for tilgangssjekking.
 * Om Journalposttypen er Notat skal NOTAT brukes, eller brukes ANNEN. Bruk metoden from(String) for å mappe fra Dokarkiv-verdier
 */
public enum TilgangJournalposttype {
	ANNEN, NOTAT;

	/**
	 * Map til TilgangJournalposttype fra verdi for journalposttype fra dokarkiv sitt api
	 *
	 * @param journalposttype verdi for journalposttype fra dokarkiv sitt api
	 * @return TilgangJournalposttype.NOTAT om journalposttype er notat, ellers ANNEN
	 */
	public static TilgangJournalposttype from(String journalposttype) {
		if ("N".equals(journalposttype)) {
			return NOTAT;
		}
		return ANNEN;
	}
}
