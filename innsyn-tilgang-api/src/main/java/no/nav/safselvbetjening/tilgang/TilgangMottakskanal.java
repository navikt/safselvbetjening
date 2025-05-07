package no.nav.safselvbetjening.tilgang;

import java.util.Set;

/**
 * TilgangMottakskanal er et enum som representerer det subsettet av mottakskanaler som er relevant for tilgangssjekking.
 * Tilgangsstyringen interessert i å vite om:
 * * journalposten har kommet inn via en av skanningskanalene og da skal verdien SKANNING brukes.
 * * journalposten har kommet inn via en av de tekniske kanalene og da skal verdien TEKNISK brukes.
 * Ellers brukes verdien IKKE_SKANNING_IKKE_TEKNISK.
 */
public enum TilgangMottakskanal {
	SKANNING, TEKNISK, IKKE_SKANNING_IKKE_TEKNISK;

	private static final Set<String> KANAL_SKANNING = Set.of("SKAN_IM", "SKAN_NETS", "SKAN_PEN");
	private static final Set<String> KANAL_TEKNISK = Set.of("ALTINN", "ALTINN_INNBOKS", "EESSI", "EIA", "EKST_OPPS", "HELSENETTET", "HR_SYSTEM_API");

	/**
	 * Map til TilgangMottakskanal fra verdi for kanal fra dokarkiv sitt api
	 *
	 * @param mottakskanal verdi for kanal fra dokarkiv sitt api
	 * @return SKANNING om dokumentet er skannet, TEKNISK om dokumentet er mottatt av en teknisk kanal. Ellers IKKE_SKANNING_IKKE_TEKNISK
	 */
	public static TilgangMottakskanal from(String mottakskanal) {
		if (mottakskanal == null) {
			return IKKE_SKANNING_IKKE_TEKNISK;
		}

		String mottakskanalUpperCase = mottakskanal.toUpperCase();
		if (KANAL_SKANNING.contains(mottakskanalUpperCase)) {
			return SKANNING;
		}

		if (KANAL_TEKNISK.contains(mottakskanalUpperCase)) {
			return TEKNISK;
		}

		return IKKE_SKANNING_IKKE_TEKNISK;
	}
}
