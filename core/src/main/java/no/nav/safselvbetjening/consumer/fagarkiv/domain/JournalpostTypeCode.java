package no.nav.safselvbetjening.consumer.fagarkiv.domain;


import no.nav.safselvbetjening.domain.Journalposttype;

public enum JournalpostTypeCode {
	/**
	 * Inngående dokument
	 */
	I(Journalposttype.I),
	/**
	 * Utgående dokument
	 */
	U(Journalposttype.U),
	/**
	 * Internt notat
	 */
	N(Journalposttype.N);

	private final Journalposttype safJournalposttype;

	JournalpostTypeCode(Journalposttype safJournalposttype) {
		this.safJournalposttype = safJournalposttype;
	}

	public Journalposttype toSafJournalposttype() {
		return safJournalposttype;
	}

	public static Journalposttype mapToJournalpostType(JournalpostTypeCode journalpostTypeCode) {
		if (journalpostTypeCode == null) {
			return null;
		}
		return Journalposttype.valueOf(journalpostTypeCode.name());
	}
}
