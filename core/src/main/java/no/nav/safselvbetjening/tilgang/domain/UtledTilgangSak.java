package no.nav.safselvbetjening.tilgang.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;

@Value
@Builder
public class UtledTilgangSak {
	private final String tema;
	private final String fagsystem;
	private final String aktoerId;
}
