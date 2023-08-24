package no.nav.safselvbetjening.audit.cef;

import lombok.Builder;
import lombok.Value;

/**
 * Implementasjon av ArcSight Common Event Format (CEF) Version 25
 */
@Value
@Builder
public class CommonEventFormat {
	Headers headers;
	Extension extension;

	@Override
	public String toString() {
		// CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
		return headers.toString() + extension.toString().trim();
	}
}
