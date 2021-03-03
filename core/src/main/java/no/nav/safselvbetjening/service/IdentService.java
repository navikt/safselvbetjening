package no.nav.safselvbetjening.service;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.pdl.IdentConsumer;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class IdentService {
    private final IdentConsumer identConsumer;

    public IdentService(IdentConsumer identConsumer) {
        this.identConsumer = identConsumer;
    }

    public BrukerIdenter hentIdenter(final String ident) {
        try {
            List<PdlResponse.PdlIdent> pdlIdenter = identConsumer.hentIdenter(ident);
            return new BrukerIdenter(pdlIdenter);
        } catch(ConsumerFunctionalException e) {
            return new BrukerIdenter(new ArrayList<>());
        }
    }
}
