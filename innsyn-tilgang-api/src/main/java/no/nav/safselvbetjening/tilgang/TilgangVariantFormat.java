package no.nav.safselvbetjening.tilgang;

import java.util.Set;

public enum TilgangVariantFormat {
	SLADDET(true),
	ARKIV(true),
	UGYLDIG_FOR_INNSYN(false);

	public static final Set<String> GYLDIGE_VARIANTER = Set.of("ARKIV", "SLADDET");

	TilgangVariantFormat(boolean gyldigForInnsyn) {
		this.gyldigForInnsyn = gyldigForInnsyn;
	}

	public final boolean gyldigForInnsyn;

	public static TilgangVariantFormat from(String value) {
		if (GYLDIGE_VARIANTER.contains(value)) {
			return valueOf(value);
		}
		return UGYLDIG_FOR_INNSYN;
	}
}
