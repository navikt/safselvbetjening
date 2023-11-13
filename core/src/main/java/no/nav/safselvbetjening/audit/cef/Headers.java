package no.nav.safselvbetjening.audit.cef;

import lombok.AllArgsConstructor;

import static java.lang.String.format;

@AllArgsConstructor
public enum Headers {
	HENT_DOKUMENT_FULLMAKT_HEADERS("AuditLog", "audit:access", "brukers dokument hentet av fullmektig"),
	DOKUMENTOVERSIKT_FULLMAKT_HEADERS("AuditLog", "audit:access", "brukers dokumentoversikt hentet av fullmektig"),
	HENT_DOKUMENT_EGEN_HEADERS("AuditLog", "audit:access", "brukers dokument hentet av bruker selv"),
	DOKUMENTOVERSIKT_EGEN_HEADERS("AuditLog", "audit:access", "dokumentoversikten til bruker hentet av bruker selv");

	final String deviceProduct;
	final String deviceEventClassID;
	final String name;

	/**
	 * CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|
	 */
	@Override
	public String toString() {
		return format("CEF:0|safselvbetjening|%s|1.0|%s|%s|INFO|", deviceProduct, deviceEventClassID, name);
	}
}