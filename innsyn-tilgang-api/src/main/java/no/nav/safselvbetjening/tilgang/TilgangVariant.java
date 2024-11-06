package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record TilgangVariant(
		@NonNull TilgangSkjermingType skjerming,
		@NonNull TilgangVariantFormat variantformat
) {
}
