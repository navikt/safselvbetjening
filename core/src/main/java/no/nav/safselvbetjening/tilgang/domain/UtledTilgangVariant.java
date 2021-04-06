package no.nav.safselvbetjening.tilgang.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;

@Value
@Builder
public class UtledTilgangVariant {
	private final SkjermingTypeCode skjerming;
}
