package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import no.nav.safselvbetjening.domain.Innsyn;

import java.util.EnumSet;
import java.util.Set;

public enum InnsynCode {
	BRUK_STANDARDREGLER(Innsyn.BRUK_STANDARDREGLER),
	VISES_MASKINELT_GODKJENT(Innsyn.VISES_MASKINELT_GODKJENT),
	VISES_MANUELT_GODKJENT(Innsyn.VISES_MANUELT_GODKJENT),
	VISES_FORVALTNINGSNOTAT(Innsyn.VISES_FORVALTNINGSNOTAT),
	SKJULES_FEILSENDT(Innsyn.SKJULES_FEILSENDT),
	SKJULES_BRUKERS_ØNSKE(Innsyn.SKJULES_BRUKERS_ØNSKE),
	SKJULES_ORGAN_INTERNT(Innsyn.SKJULES_ORGAN_INTERNT),
	SKJULES_INNSKRENKET_PARTSINNSYN(Innsyn.SKJULES_INNSKRENKET_PARTSINNSYN);

	private final Innsyn safInnsyn;

	InnsynCode(Innsyn safInnsyn) {
		this.safInnsyn = safInnsyn;
	}

	public Innsyn toSafInnsyn() {
		return safInnsyn;
	}

	public static Innsyn mapToInnsyn(InnsynCode innsynCode) {
		if (innsynCode == null) {
			return null;
		}
		return Innsyn.valueOf(innsynCode.name());
	}

	public static Set<InnsynCode> getInnsynStartWithSkjules() {
		return EnumSet.of(SKJULES_FEILSENDT, SKJULES_BRUKERS_ØNSKE, SKJULES_ORGAN_INTERNT, SKJULES_INNSKRENKET_PARTSINNSYN);
	}

	public static Set<InnsynCode> getInnsynStartWithVises() {
		return EnumSet.of(VISES_MASKINELT_GODKJENT, VISES_MANUELT_GODKJENT, VISES_FORVALTNINGSNOTAT);
	}

}
