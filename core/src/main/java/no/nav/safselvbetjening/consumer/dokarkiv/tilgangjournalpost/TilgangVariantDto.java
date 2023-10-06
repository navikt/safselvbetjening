package no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode;

@Value
@Builder
public class TilgangVariantDto {
	VariantFormatCode variantFormat;
	SkjermingTypeCode skjerming;
}
