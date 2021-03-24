package no.nav.safselvbetjening;

import org.slf4j.MDC;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class MDCUtils {
	public static final String MDC_CALL_ID = "callId";

	public static String getCallId() {
		final String callId = MDC.get(MDC_CALL_ID);
		return isBlank(callId) ? UUID.randomUUID().toString() : callId;
	}

	private MDCUtils() {
		// ingen instansiering
	}
}
