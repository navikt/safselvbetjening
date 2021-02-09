package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import no.nav.safselvbetjening.domain.Kanal;

public enum UtsendingsKanalCode {
	EESSI(Kanal.EESSI),
	ALTINN(Kanal.ALTINN),
	NAV_NO(Kanal.NAV_NO),
	S(Kanal.SENTRAL_UTSKRIFT),
	L(Kanal.LOKAL_UTSKRIFT),
	SDP(Kanal.SDP),
	EIA(Kanal.EIA),
	INGEN_DISTRIBUSJON(Kanal.INGEN_DISTRIBUSJON),
	TRYGDERETTEN(Kanal.TRYGDERETTEN),
	HELSENETTET(Kanal.HELSENETTET);

	private final Kanal safKanal;

	UtsendingsKanalCode(Kanal safKanal) {
		this.safKanal = safKanal;
	}

	public Kanal getSafKanal() {
		return safKanal;
	}
}
