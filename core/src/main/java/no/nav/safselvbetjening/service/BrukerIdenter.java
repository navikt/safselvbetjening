package no.nav.safselvbetjening.service;

import no.nav.safselvbetjening.consumer.pdl.PdlResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class BrukerIdenter {
    private final List<String> aktoerIds = new ArrayList<>();
    private final List<String> foedselsnummer = new ArrayList<>();

    public BrukerIdenter(final List<PdlResponse.PdlIdent> pdlIdenter) {
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

    public List<String> getAktoerIds() {
        return Collections.unmodifiableList(aktoerIds);
    }

    public List<String> getFoedselsnummer() {
        return Collections.unmodifiableList(foedselsnummer);
    }

    public List<String> getIdenter() {
    	return Stream.concat(getAktoerIds().stream(), getFoedselsnummer().stream()).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return aktoerIds.isEmpty() && foedselsnummer.isEmpty();
    }
}
