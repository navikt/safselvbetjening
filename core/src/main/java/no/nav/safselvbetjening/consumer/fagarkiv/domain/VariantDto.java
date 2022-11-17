package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VariantDto {
	VariantFormatCode variantf;
	String filnavn;
	String filuuid;
	String filtype;
	String filstorrelse;
	SkjermingTypeCode skjerming;
}
