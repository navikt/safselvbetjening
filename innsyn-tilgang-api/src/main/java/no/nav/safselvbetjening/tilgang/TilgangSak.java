package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract sealed class TilgangSak permits TilgangGosysSak, TilgangPensjonSak {
	private final String tema;
	private final TilgangFagsystem fagsystem;
	private final boolean feilregistrert;
}

