package no.nav.safselvbetjening.tilgang;

public enum TilgangJournalstatus {
	MOTTATT,
	FERDIGSTILT,
	ANNEN;

	public static TilgangJournalstatus from(String journalstatus) {
		return switch (journalstatus) {
			case "M", "MO" -> TilgangJournalstatus.MOTTATT;
			case "J", "E", "FS", "FL" -> TilgangJournalstatus.FERDIGSTILT;
			default -> TilgangJournalstatus.ANNEN;
		};
	}
}
