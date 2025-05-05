package no.nav.safselvbetjening.tilgang;

import java.util.Set;

/**
 * TilgangUtsendingskanal er et enum som representerer det subsettet av utsendingskanal som er relevant for tilgangssjekking.
 * Tilgangsstyringen interessert i å vite om:
 * * journalposten har sendt ut fra en av de tekniske kanalene og da skal verdien TEKNISK brukes.
 * Ellers brukes verdien IKKE_TEKNISK.
 */
public enum TilgangUtsendingskanal {
	TEKNISK, IKKE_TEKNISK;

	private static final Set<String> KANAL_TEKNISK = Set.of("ALTINN", "EESSI", "EIA", "HELSENETTET", "TRYGDERETTEN");

	/**
	 * Map til TilgangUtsendingskanal fra verdi for kanal fra dokarkiv sitt api
	 *
	 * @param utsendingskanal verdi for kanal fra dokarkiv sitt api
	 * @return TEKNISK om dokumentet er sendt fra en teknisk kanal. Ellers IKKE_TEKNISK
	 */
	public static TilgangUtsendingskanal from(String utsendingskanal) {
		if (utsendingskanal == null) {
			return IKKE_TEKNISK;
		}

		if (KANAL_TEKNISK.contains(utsendingskanal.toUpperCase())) {
			return TEKNISK;
		}

		return IKKE_TEKNISK;
	}
}
