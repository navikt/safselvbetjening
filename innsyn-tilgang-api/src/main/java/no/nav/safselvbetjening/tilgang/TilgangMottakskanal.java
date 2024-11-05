package no.nav.safselvbetjening.tilgang;

import java.util.Set;

public enum TilgangMottakskanal {
	SKANNING, IKKE_SKANNING;

	private static final Set<String> KANAL_SKANNING = Set.of("SKAN_IM", "SKAN_NETS", "SKAN_PEN");

	public static TilgangMottakskanal from(String kanal) {
		if (kanal != null && KANAL_SKANNING.contains(kanal.toUpperCase())) {
			return SKANNING;
		}
		return IKKE_SKANNING;
	}
}
