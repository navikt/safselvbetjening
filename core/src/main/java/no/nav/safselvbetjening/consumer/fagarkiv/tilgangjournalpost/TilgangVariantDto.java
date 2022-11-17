package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;

@Value
@Builder
public class TilgangVariantDto {
	VariantFormatCode variantFormat;
	SkjermingTypeCode skjerming;
}
