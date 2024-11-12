package no.nav.safselvbetjening.tilgang;

import java.util.EnumSet;

/**
 * TilgangInnsyn er et enum som representerer hvilke innsynsregeler som gjelder for journalposten.
 * Bruk from(String) for å mappe fra verdiene fra Dokarkiv
 */
public enum TilgangInnsyn {
	VISES_MASKINELT_GODKJENT, VISES_MANUELT_GODKJENT, VISES_FORVALTNINGSNOTAT,
	SKJULES_BRUKERS_ONSKE, SKJULES_INNSKRENKET_PARTSINNSYN, SKJULES_FEILSENDT, SKJULES_ORGAN_INTERNT, SKJULES_BRUKERS_SIKKERHET,
	BRUK_STANDARDREGLER;

	public static final EnumSet<TilgangInnsyn> VISES = EnumSet.of(VISES_MASKINELT_GODKJENT, VISES_MANUELT_GODKJENT, VISES_FORVALTNINGSNOTAT);
	public static final EnumSet<TilgangInnsyn> SKJULES = EnumSet.of(SKJULES_BRUKERS_ONSKE, SKJULES_INNSKRENKET_PARTSINNSYN, SKJULES_FEILSENDT, SKJULES_ORGAN_INTERNT, SKJULES_BRUKERS_SIKKERHET);

	/**
	 * Map til TilgangInnsyn fra verdi for innsyn fra dokarkiv sitt api
	 *
	 * @param innsynNavn verdi for innsyn fra dokarkiv sitt api
	 * @return TilgangInnsyn som representerer verdien fra dokarkiv
	 */
	public static TilgangInnsyn from(String innsynNavn) {
		if (innsynNavn == null) {
			return BRUK_STANDARDREGLER;
		}
		return valueOf(innsynNavn.toUpperCase());
	}
}
