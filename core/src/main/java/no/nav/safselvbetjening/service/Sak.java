package no.nav.safselvbetjening.service;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Sak {
    private final String arkivsakId;
    private final String tema;
}
