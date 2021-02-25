package no.nav.safselvbetjening.consumer.pdl;

import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;

import java.util.List;

/**
 * Interface for tjenester relatert til henting av identer.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface IdentConsumer {
    /**
     * Henter alle identer NAV har på en bruker.
     *
     * @param ident Ident tilhørende person
     * @return NAV identer. Både folkeregisteridenter og aktørid.
     * @throws PersonIkkeFunnetException Finner ikke person
     */
    List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException;
}
