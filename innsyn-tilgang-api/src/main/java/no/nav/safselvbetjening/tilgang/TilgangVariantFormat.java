package no.nav.safselvbetjening.tilgang;

import java.util.Set;

/**
 * TilgangVariantFormat er et enum som inneholder variantFormat som er relevante for tilgangskontroll. De eneste
 * variantformatene som er tillatt for å gi innsyn i er SLADDET og ARKIV. Andre varianter skal mappes til UGYLDIG_FOR_INNSYN.
 * Bruk from(String) for å mappe fra Dokarkiv-verdier
 */
public enum TilgangVariantFormat {
	SLADDET(true),
	ARKIV(true),
	UGYLDIG_FOR_INNSYN(false);

	public static final Set<String> GYLDIGE_VARIANTER = Set.of("ARKIV", "SLADDET");

	TilgangVariantFormat(boolean gyldigForInnsyn) {
		this.gyldigForInnsyn = gyldigForInnsyn;
	}

	public final boolean gyldigForInnsyn;

	/**
	 * Map til TilgangVariantFormat fra verdi for variantformat fra dokarkiv sitt api
	 *
	 * @param variantformat verdi for skjerming fra dokarkiv sitt api
	 * @return TilgangVariantFormat som korresponderer med verdi fra dokarkiv sitt api. UGYLDIG_FOR_INNSYN om
	 * variantformatet ikke kan vises gjennom innsynsløsningen
	 */
	public static TilgangVariantFormat from(String variantformat) {
		if (GYLDIGE_VARIANTER.contains(variantformat)) {
			return valueOf(variantformat);
		}
		return UGYLDIG_FOR_INNSYN;
	}
}
