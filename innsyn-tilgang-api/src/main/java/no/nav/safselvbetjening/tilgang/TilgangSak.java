package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * TilgangSak er en representasjon av en Sak i et av fagsystemene. Ident kan representere en AktørId, et fødselsnummer eller et organisasjonsnummer
 */
@Getter
@SuperBuilder
public class TilgangSak {
	private final String tema;
	private final boolean feilregistrert;
	private final Ident ident;
}
