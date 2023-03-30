package no.nav.safselvbetjening.consumer.pdl;

import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse.PdlIdent;

import java.util.List;

/**
 * Interface for tjenester relatert til henting av identer.
 */
public interface IdentConsumer {
    /**
     * Henter alle identer NAV har på en bruker.
     *
     * @param ident Ident tilhørende person
     * @return NAV identer. Både folkeregisteridenter og aktørid.
     * @throws PersonIkkeFunnetException Finner ikke person
     */
    List<PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException;
}
