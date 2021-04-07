package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Builder
@Data
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final TilgangVariant tilgangVariant;
	@Setter
	private boolean brukerHarTilgang;
	@Builder.Default
	private List<String> code = new ArrayList<>();

	@Data
	@Builder
	public static class TilgangVariant {
		private final SkjermingType skjerming;
	}
}
