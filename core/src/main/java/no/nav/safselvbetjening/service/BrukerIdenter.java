package no.nav.safselvbetjening.service;

import lombok.Getter;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
public class BrukerIdenter {
    private final List<String> aktoerIds = new ArrayList<>();
    private final List<String> foedselsnummer = new ArrayList<>();

    BrukerIdenter(final List<PdlResponse.PdlIdent> pdlIdenter) {
        for(PdlResponse.PdlIdent pdlIdent : pdlIdenter) {
            switch(pdlIdent.getGruppe()) {
                case AKTORID:
                    this.aktoerIds.add(pdlIdent.getIdent());
                    break;
                case FOLKEREGISTERIDENT:
                    this.foedselsnummer.add(pdlIdent.getIdent());
                    break;
                default:
                    // noop
                    break;
            }
        }
    }

    public boolean isEmpty() {
        return aktoerIds.isEmpty() && foedselsnummer.isEmpty();
    }
}
