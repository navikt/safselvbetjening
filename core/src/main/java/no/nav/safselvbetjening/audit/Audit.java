package no.nav.safselvbetjening.audit;

import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;

public interface Audit {
	Logger auditLog = LoggerFactory.getLogger("auditLog");

	Clock clock();

	default void log(CommonEventFormat cef) {
		if (cef == null) {
			return;
		}
		auditLog.info(cef.toString());
	}
}
