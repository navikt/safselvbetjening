package no.nav.safselvbetjening.hentdokument.audit.cef;

import lombok.Builder;
import lombok.Value;

import static java.lang.String.format;

@Value
@Builder
public class Headers {
	String deviceProduct;
	String deviceEventClassID;
	String name;

	/**
	 * CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|
	 */
	@Override
	public String toString() {
		return format("CEF:0|safselvbetjening|%s|1.0|%s|%s|Low|", getDeviceProduct(), getDeviceEventClassID(), getName());
	}

	public static Headers hentdokumentFullmaktHeaders() {
		return new Headers("hentdokument", "audit:access", "brukers dokument hentet av fullmektig");
	}
}