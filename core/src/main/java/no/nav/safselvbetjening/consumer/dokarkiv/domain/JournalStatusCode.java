package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;

import java.util.EnumSet;
import java.util.Set;

public enum JournalStatusCode {
	/**
	 * journalført
	 */
	J(Journalstatus.JOURNALFOERT, TilgangJournalstatus.JOURNALFOERT),
	/**
	 * midl journalført
	 */
	M(Journalstatus.MOTTATT, TilgangJournalstatus.MOTTATT),
	/**
	 * Utgår før tilknytn til sak
	 */
	U(Journalstatus.UTGAAR, TilgangJournalstatus.UTGAAR),
	/**
	 * Dokument under produksjon
	 */
	D(Journalstatus.UNDER_ARBEID, TilgangJournalstatus.UNDER_ARBEID),
	/**
	 * Reservert dokument
	 */
	R(Journalstatus.RESERVERT, TilgangJournalstatus.RESERVERT),
	/**
	 * Ferdig og sentral print
	 */
	FS(Journalstatus.FERDIGSTILT, TilgangJournalstatus.FERDIGSTILT),
	/**
	 * Ferdig og lokal print
	 */
	FL(Journalstatus.FERDIGSTILT, TilgangJournalstatus.FERDIGSTILT),
	/**
	 * Ekspedert
	 */
	E(Journalstatus.EKSPEDERT, TilgangJournalstatus.EKSPEDERT),
	/**
	 * Avbrutt
	 */
	A(Journalstatus.AVBRUTT, TilgangJournalstatus.AVBRUTT),
	/**
	 * Mottatt
	 */
	MO(Journalstatus.MOTTATT, TilgangJournalstatus.MOTTATT),
	/**
	 * Ukjent bruker
	 */
	UB(Journalstatus.UKJENT_BRUKER, TilgangJournalstatus.UKJENT_BRUKER),
	/**
	 * Opplasting dokument
	 */
	OD(Journalstatus.OPPLASTING_DOKUMENT, TilgangJournalstatus.OPPLASTING_DOKUMENT);

	private final Journalstatus safJournalstatus;
	private final TilgangJournalstatus tilgangJournalstatus;

	JournalStatusCode(Journalstatus safJournalstatus, TilgangJournalstatus tilgangJournalstatus) {
		this.safJournalstatus = safJournalstatus;
		this.tilgangJournalstatus = tilgangJournalstatus;
	}

	public Journalstatus toSafJournalstatus() {
		return safJournalstatus;
	}

	public static Set<JournalStatusCode> getJournalstatusFerdigstilt() {
		return EnumSet.of(FL, FS, J, E);
	}

	public static Set<JournalStatusCode> getJournalstatusMidlertidig() {
		return EnumSet.of(M, MO);
	}

	public TilgangJournalstatus toTilgangJournalstatus() {
		return tilgangJournalstatus;
	}
}
