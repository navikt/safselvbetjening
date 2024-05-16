package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import java.util.Arrays;
import java.util.List;

public enum InnsynCode {
	BRUK_STANDARDREGLER,
	VISES_MASKINELT_GODKJENT,
	VISES_MANUELT_GODKJENT,
	VISES_FORVALTNINGSNOTAT,
	SKJULES_FEILSENDT,
	SKJULES_BRUKERS_ONSKE,
	SKJULES_ORGAN_INTERNT,
	SKJULES_INNSKRENKET_PARTSINNSYN,
	SKJULES_BRUKERS_SIKKERHET;

	public static List<InnsynCode> asList() {
		return Arrays.asList(values());
	}
}
