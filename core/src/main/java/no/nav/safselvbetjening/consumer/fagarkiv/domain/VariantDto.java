package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class VariantDto {
	private VariantFormatCode variantf;
	private String filnavn;
	private String filuuid;
	private String filtype;
	private String filstorrelse;
	private SkjermingTypeCode skjerming;
}
