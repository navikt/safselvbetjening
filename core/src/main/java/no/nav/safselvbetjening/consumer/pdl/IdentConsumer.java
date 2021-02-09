package no.nav.safselvbetjening.consumer.pdl;

import java.util.List;

/**
 * Interface for tjenester relatert til henting av identer.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface IdentConsumer {
    /**
     * Henter NAV intern aktørIder for folkeregisterIdent.
     *
     * @param folkeregisterIdent Folkeregisterident tilhørende person
     * @return NAV intern aktørIder. En bruker kan ha hatt flere aktørId
     * @throws PersonIkkeFunnetException Finner ikke person
     */
    List<String> hentAktoerIder(final String folkeregisterIdent) throws PersonIkkeFunnetException;

    /**
     * Henter Folkeregisterets fødselsnummer for NAV intern aktørId
     *
     * @param aktoerId NAV intern aktørId
     * @return Folkeregister identer. En bruker kan ha flere fødselsnummer.
     * @throws PersonIkkeFunnetException Finner ikke person
     */
    List<String> hentFolkeregisterIdenter(final String aktoerId) throws PersonIkkeFunnetException;
}
