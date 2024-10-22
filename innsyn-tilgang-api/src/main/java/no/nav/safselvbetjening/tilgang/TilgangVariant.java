package no.nav.safselvbetjening.tilgang;

import lombok.Builder;

@Builder
public record TilgangVariant(
		TilgangSkjermingType skjerming,
		TilgangVariantFormat variantformat
) {
}
