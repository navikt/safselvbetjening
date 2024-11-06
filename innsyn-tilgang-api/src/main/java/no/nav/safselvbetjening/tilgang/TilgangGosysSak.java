package no.nav.safselvbetjening.tilgang;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public final class TilgangGosysSak extends TilgangSak {
	AktoerId aktoerId;
}
