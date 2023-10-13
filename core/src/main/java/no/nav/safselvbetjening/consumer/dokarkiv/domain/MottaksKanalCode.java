package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import no.nav.safselvbetjening.domain.Kanal;

public enum MottaksKanalCode {
	EESSI(Kanal.EESSI),
	EIA(Kanal.EIA),
	NAV_NO(Kanal.NAV_NO),
	ALTINN(Kanal.ALTINN),
	SKAN_PEN(Kanal.SKAN_PEN),
	SKAN_NETS(Kanal.SKAN_NETS),
	SKAN_IM(Kanal.SKAN_IM),
	EKST_OPPS(Kanal.EKST_OPPS),
	HELSENETTET(Kanal.HELSENETTET),
	NAV_NO_UINNLOGGET(Kanal.NAV_NO_UINNLOGGET),
	INNSENDT_NAV_ANSATT(Kanal.INNSENDT_NAV_ANSATT),
	NAV_NO_CHAT(Kanal.NAV_NO_CHAT);

	private final Kanal safKanal;

	MottaksKanalCode(Kanal safKanal) {
		this.safKanal = safKanal;
	}

	public Kanal getSafKanal() {
		return safKanal;
	}
}
