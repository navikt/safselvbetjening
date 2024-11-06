package no.nav.safselvbetjening.tilgang;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public final class TilgangPensjonSak extends TilgangSak {
	Foedselsnummer foedselsnummer;
}
