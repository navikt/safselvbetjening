package no.nav.safselvbetjening.tilgang;

import lombok.Builder;

@Builder
public record TilgangSak(
		String tema,
		TilgangFagsystem fagsystem,
		// Populert for arkivsaksystem gsak
		String aktoerId,
		// Populert for arkivsaksystem pensjon
		String foedselsnummer,
		boolean feilregistrert
) {
}
