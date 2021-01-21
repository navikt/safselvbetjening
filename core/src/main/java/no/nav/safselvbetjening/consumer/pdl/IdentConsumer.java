package no.nav.safselvbetjening.consumer.pdl;

import java.util.List;

/**
 * Interface for tjenester relatert til henting av identer.
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public interface IdentConsumer {
	/**
	 * Henter NAV intern aktørId for folkeregisterIdent.
	 *
	 * @param folkeregisterIdent Folkeregisterident tilhørende person
	 * @return NAV intern aktørId
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	String hentAktoerId(final String folkeregisterIdent) throws PersonIkkeFunnetException;

	/**
	 * Henter Folkeregisterets fødselsnummer for NAV intern aktørId
	 *
	 * @param aktoerId NAV intern aktørId
	 * @return Folkeregister ident
	 * @throws PersonIkkeFunnetException Finner ikke person
	 */
	String hentFolkeregisterIdent(final String aktoerId) throws PersonIkkeFunnetException;
}
