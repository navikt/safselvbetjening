package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Builder
@Data
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final int filstorrelse;
	private final TilgangVariant tilgangVariant;
	private boolean brukerHarTilgang;
	@Builder.Default
	private List<String> code = new ArrayList<>();

	@Data
	@Builder
	public static class TilgangVariant {
		private final SkjermingType skjerming;
	}
}
