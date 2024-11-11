package no.nav.safselvbetjening.tilgang;

import java.util.Set;

/**
 * TilgangMottakskanal er et enum som representerer det subsettet av mottakskanaler som er relevant for tilgangssjekking.
 * Tilgangsstyringen er kun interessert i å vite om journalposten har kommet inn via en av skanningskanalene eller ikke,
 * og da skal verdien SKANNING brukes. Ellers brukes verdien IKKE_SKANNING.
 * Bruk metoden from(String) for å mappe fra Dokarkiv-verdier
 */
public enum TilgangMottakskanal {
	SKANNING, IKKE_SKANNING;

	private static final Set<String> KANAL_SKANNING = Set.of("SKAN_IM", "SKAN_NETS", "SKAN_PEN");

	/**
	 * Map til TilgangMottakskanal fra verdi for kanal fra dokarkiv sitt api
	 *
	 * @param kanal verdi for kanal fra dokarkiv sitt api
	 * @return TilgangMottakskanal.SKANNING om dokumentet er skannet, IKKE_SKANNING ellers
	 */
	public static TilgangMottakskanal from(String kanal) {
		if (kanal != null && KANAL_SKANNING.contains(kanal.toUpperCase())) {
			return SKANNING;
		}
		return IKKE_SKANNING;
	}
}
