package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;

public record ArkivFildetaljer(String skjerming,
							   String format,
							   // kun metadata, ikke brukt til tilgangskontroll
							   String stoerrelse,
							   String type,
							   String uuid
) {

	public TilgangVariant getTilgangVariant() {
		return TilgangVariant.builder()
				.variantformat(TilgangVariantFormat.from(format))
				.skjerming(TilgangSkjermingType.from(skjerming))
				.build();
	}
}
