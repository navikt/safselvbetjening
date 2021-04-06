package no.nav.safselvbetjening.tilgang.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UtledTilgangBruker {
	private final String brukerId;
}
