package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import no.nav.safselvbetjening.domain.Journalstatus;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum JournalStatusCode {
	/**
	 * journalført
	 */
	J(Journalstatus.JOURNALFOERT),
	/**
	 * midl journalført
	 */
	M(Journalstatus.MOTTATT),
	/**
	 * Utgår før tilknytn til sak
	 */
	U(Journalstatus.UTGAAR),
	/**
	 * Dokument under produksjon
	 */
	D(Journalstatus.UNDER_ARBEID),
	/**
	 * Reservert dokument
	 */
	R(Journalstatus.RESERVERT),
	/**
	 * Ferdig og sentral print
	 */
	FS(Journalstatus.FERDIGSTILT),
	/**
	 * Ferdig og lokal print
	 */
	FL(Journalstatus.FERDIGSTILT),
	/**
	 * Ekspedert
	 */
	E(Journalstatus.EKSPEDERT),
	/**
	 * Avbrutt
	 */
	A(Journalstatus.AVBRUTT),
	/**
	 * Mottatt   
	 */
	MO(Journalstatus.MOTTATT),
	/**
	 * Ukjent bruker 
	 */
	UB(Journalstatus.UKJENT_BRUKER),
	/**
	 * Opplasting dokument 
	 */
	OD(Journalstatus.OPPLASTING_DOKUMENT);

	private final Journalstatus safJournalstatus;

	JournalStatusCode(Journalstatus safJournalstatus) {
		this.safJournalstatus = safJournalstatus;
	}

	public Journalstatus toSafJournalstatus() {
		return safJournalstatus;
	}

	public static List<JournalStatusCode> asList() {
		return Arrays.asList(values());
	}

	public static Set<JournalStatusCode> getJournalstatusFerdigstilt() {
		return EnumSet.of(FL, FS, J, E);
	}

	public static Set<JournalStatusCode> getJournalstatusMidlertidig() {
		return EnumSet.of(M, MO);
	}
}
