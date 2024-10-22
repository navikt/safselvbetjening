package no.nav.safselvbetjening.tilgang;

import java.util.EnumSet;
import java.util.Set;

public enum TilgangJournalstatus {
	MOTTATT,
	JOURNALFOERT,
	FERDIGSTILT,
	EKSPEDERT,
	UNDER_ARBEID,
	FEILREGISTRERT,
	UTGAAR,
	AVBRUTT,
	UKJENT_BRUKER,
	RESERVERT,
	OPPLASTING_DOKUMENT,
	UKJENT;

	public static final Set<TilgangJournalstatus> FERDIGSTILT_STATUS = EnumSet.of(FERDIGSTILT, JOURNALFOERT, EKSPEDERT);
}
