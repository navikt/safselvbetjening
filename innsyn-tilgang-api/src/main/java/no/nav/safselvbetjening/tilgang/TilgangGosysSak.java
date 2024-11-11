package no.nav.safselvbetjening.tilgang;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * En TilgangGosysSak representerer en sak i fagsystemet Gosys (FS22)
 *
 * @see TilgangSak
 */
@Data
@SuperBuilder
public final class TilgangGosysSak extends TilgangSak {
	private final TilgangFagsystem fagsystem = TilgangFagsystem.GOSYS;
	AktoerId aktoerId;
}
