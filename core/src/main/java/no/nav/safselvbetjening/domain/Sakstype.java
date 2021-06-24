package no.nav.safselvbetjening.domain;

public enum Sakstype {
	FAGSAK,
	GENERELL_SAK;

	public static final String APPLIKASJON_GENERELL_SAK = "FS22";

	public static Sakstype fromApplikasjon(String applikasjon) {
		return applikasjon == null || APPLIKASJON_GENERELL_SAK.equals(applikasjon) ? GENERELL_SAK : FAGSAK;
	}

}
