package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final boolean brukerHarTilgang;
	private final String code;
}
