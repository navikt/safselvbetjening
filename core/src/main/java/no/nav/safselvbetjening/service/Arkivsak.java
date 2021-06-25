package no.nav.safselvbetjening.service;

import lombok.Builder;
import lombok.Value;

import static no.nav.safselvbetjening.domain.Sakstype.APPLIKASJON_GENERELL_SAK;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Value
@Builder
public class Arkivsak {
    private final String arkivsakId;
    private final String tema;
    private final String fagsakId;
    private final String fagsaksystem;

    public String getFagSakIdAndFagsaksystem() {
        if(isBlank(fagsakId) && isBlank(fagsaksystem)) {
            return null;
        }
        return fagsakId + "_" + fagsaksystem;
    }

    public boolean isFagsak() {
        if(isBlank(fagsakId) && isBlank(fagsaksystem)) {
            return false;
        }
        return !APPLIKASJON_GENERELL_SAK.equals(fagsaksystem);
    }
}
