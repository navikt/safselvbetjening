package no.nav.safselvbetjening.tilgang;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * TilgangPensjonSak representerer en sak i fagsystemet til Pensjon
 */
@Data
@SuperBuilder
public final class TilgangPensjonSak extends TilgangSak {
	private final TilgangFagsystem fagsystem = TilgangFagsystem.PENSJON;
	Foedselsnummer foedselsnummer;
}
