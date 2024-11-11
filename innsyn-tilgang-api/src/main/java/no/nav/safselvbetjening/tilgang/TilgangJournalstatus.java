package no.nav.safselvbetjening.tilgang;

/**
 * TilgangJournalstatus er et enum som representerer det subsettet av Journalstatuser som er relevant for tilgangssjekking.
 * Tilgangsstyringenuer interessert i to forskjellige grupper av tilstander: Ferdigstilte journalposter som er ferdig behandlet,
 * og Mottatte journalposter som er sendt av brukeren. Bruk metoden from(String) for å mappe fra Dokarkiv-verdier
 */
public enum TilgangJournalstatus {
	MOTTATT,
	FERDIGSTILT,
	ANNEN;

	/**
	 * Map til TilgangJournalstatus fra verdi for journalstatus fra dokarkiv sitt api
	 *
	 * @param journalstatus verdi for journalstatus fra dokarkiv sitt api
	 * @return TilgangJournalstatus som korresponderer med journalstatus fra dokarkiv
	 */
	public static TilgangJournalstatus from(String journalstatus) {
		return switch (journalstatus) {
			case "M", "MO" -> TilgangJournalstatus.MOTTATT;
			case "J", "E", "FS", "FL" -> TilgangJournalstatus.FERDIGSTILT;
			default -> TilgangJournalstatus.ANNEN;
		};
	}
}
