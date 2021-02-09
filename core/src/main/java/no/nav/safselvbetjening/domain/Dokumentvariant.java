package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;


@Value
@Builder
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final boolean brukerHarTilgang;
	@Builder.Default
	private final List<String> code;
}
