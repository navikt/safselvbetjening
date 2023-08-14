package no.nav.safselvbetjening.hentdokument.audit;

import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;
import no.nav.safselvbetjening.hentdokument.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.hentdokument.audit.cef.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;

import static java.lang.String.join;

public final class Audit {
	private static final Logger auditLog = LoggerFactory.getLogger("auditLog");
	private final Clock clock;

	public Audit(Clock clock) {
		this.clock = clock;
	}

	public void logHentDokumentSomFullmektig(Fullmakt fullmakt, HentdokumentRequest hentdokumentRequest) {
		log(mapHentDokument(fullmakt, hentdokumentRequest));
	}

	CommonEventFormat mapHentDokument(Fullmakt fullmakt, HentdokumentRequest hentdokumentRequest) {
		return CommonEventFormat.builder()
				.headers(Headers.hentdokumentFullmaktHeaders())
				.extension(HentDokumentExtension.builder()
						.clock(clock)
						.sourceUserId(fullmakt.fullmektig())
						.sourceUserPrivileges("fullmektig[" + join(",", fullmakt.tema()) + "]")
						.deviceAction("hentdokument_fullmektig")
						.destinationUserId(fullmakt.fullmaktsgiver())
						.hentdokumentRequest(hentdokumentRequest)
						.build())
				.build();
	}

	static void log(CommonEventFormat cef) {
		if (cef == null) {
			return;
		}
		auditLog.info(cef.toString());
	}
}
