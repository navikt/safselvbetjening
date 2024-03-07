package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import java.util.EnumSet;

/**
 * Enum for codes in T_K_BEGRENSNING_TYPE.
 */
public enum SkjermingTypeCode {
    POL,
    FEIL;

    public static EnumSet<SkjermingTypeCode> asList() {
        return EnumSet.of(POL, FEIL);
    }
}
