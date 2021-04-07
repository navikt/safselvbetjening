package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Value
@Builder
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final boolean brukerHarTilgang;
	@Builder.Default
	private final List<String> code = new ArrayList<>();
	private final TilgangVariant tilgangVariant;

	@Data
	@Builder
	public static class TilgangVariant {
		private final SkjermingType skjerming;
	}
}
