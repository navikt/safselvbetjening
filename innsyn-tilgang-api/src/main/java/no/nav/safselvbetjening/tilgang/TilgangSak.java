package no.nav.safselvbetjening.tilgang;

import lombok.Builder;

@Builder
public record TilgangSak(
		String tema,
		TilgangFagsystem fagsystem,
		// Populert for arkivsaksystem gsak
		AktoerId aktoerId,
		// Populert for arkivsaksystem pensjon
		Foedselsnummer foedselsnummer,
		boolean feilregistrert
) {
}
