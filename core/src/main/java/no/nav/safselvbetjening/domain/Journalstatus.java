package no.nav.safselvbetjening.domain;

import java.util.EnumSet;

public enum Journalstatus {
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

	public static EnumSet<Journalstatus> ferdigstilt() {
		return EnumSet.of(FERDIGSTILT, JOURNALFOERT, EKSPEDERT);
	}

}
