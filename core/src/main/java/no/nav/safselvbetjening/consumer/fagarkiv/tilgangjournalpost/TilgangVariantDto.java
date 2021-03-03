package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class TilgangVariantDto {
	private VariantFormatCode variantFormat;
	private SkjermingTypeCode skjerming;
}
