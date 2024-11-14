package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * TilgangSak er en abstrakt representasjon av en Sak i et av fagsystemene. Ved mapping av data fra Dokarkiv må en av de
 * konkrete implementasjonene brukes.
 *
 * @see TilgangGosysSak
 * @see TilgangPensjonSak
 */
@Getter
@SuperBuilder
public abstract sealed class TilgangSak permits TilgangGosysSak, TilgangPensjonSak {
	private final String tema;
	private final boolean feilregistrert;

	public abstract TilgangFagsystem getFagsystem();
}

