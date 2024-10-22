package no.nav.safselvbetjening.domain;

public enum Kanal {
	ALTINN("Altinn"),
	EESSI("EESSI"),
	EIA("EIA"),
	EKST_OPPS("Eksternt oppslag"),
	LOKAL_UTSKRIFT("Lokal utskrift"),
	NAV_NO("Ditt NAV"),
	SENTRAL_UTSKRIFT("Sentral utskrift"),
	SDP("Digital postkasse til innbyggere"),
	SKAN_NETS("Skanning Nets"),
	SKAN_PEN("Skanning Pensjon"),
	SKAN_IM("Skanning Iron Mountain"),
	TRYGDERETTEN("Trygderetten"),
	HELSENETTET("Helsenettet"),
	INGEN_DISTRIBUSJON("Ingen distribusjon"),
	UKJENT("Ukjent"),
	NAV_NO_UINNLOGGET("Ditt NAV uten ID-porten-pålogging"),
	INNSENDT_NAV_ANSATT("Registrert av Nav-ansatt"),
	NAV_NO_CHAT("Innlogget samtale"),
	DPV("Taushetsbelagt Post via Altinn"),
	E_POST("E-post"),
	ALTINN_INNBOKS("Altinn Innboks");

	private final String kanalnavn;

	Kanal(String kanalnavn) {
		this.kanalnavn = kanalnavn;
	}

	public String getKanalnavn() {
		return kanalnavn;
	}
}
