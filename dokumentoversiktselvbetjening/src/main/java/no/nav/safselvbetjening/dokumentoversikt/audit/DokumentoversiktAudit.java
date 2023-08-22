package no.nav.safselvbetjening.dokumentoversikt.audit;

import no.nav.safselvbetjening.audit.Audit;
import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.audit.cef.Headers;
import no.nav.safselvbetjening.fullmektig.Fullmakt;

import java.time.Clock;

import static java.lang.String.join;

public record DokumentoversiktAudit(Clock clock) implements Audit {

	public void logSomFullmektig(Fullmakt fullmakt) {
		log(mapDokumentoversikt(fullmakt));
	}

	CommonEventFormat mapDokumentoversikt(Fullmakt fullmakt) {
		return CommonEventFormat.builder()
				.headers(Headers.dokumentoversiktFullmaktHeaders())
				.extension(DokumentoversiktExtension.builder()
						.clock(clock())
						.sourceUserId(fullmakt.fullmektig())
						.sourceUserPrivileges("fullmektig[" + join(",", fullmakt.tema()) + "]")
						.deviceAction("dokumentoversikt_fullmektig")
						.destinationUserId(fullmakt.fullmaktsgiver())
						.build())
				.build();
	}
}
