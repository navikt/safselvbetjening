package no.nav.safselvbetjening.hentdokument.audit;

import no.nav.safselvbetjening.audit.Audit;
import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.audit.cef.Headers;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;

import java.time.Clock;

import static java.lang.String.join;

public record HentDokumentAudit(Clock clock) implements Audit {

	public void logSomFullmektig(Fullmakt fullmakt, HentdokumentRequest hentdokumentRequest) {
		log(mapHentDokument(fullmakt, hentdokumentRequest));
	}

	CommonEventFormat mapHentDokument(Fullmakt fullmakt, HentdokumentRequest hentdokumentRequest) {
		return CommonEventFormat.builder()
				.headers(Headers.hentdokumentFullmaktHeaders())
				.extension(HentDokumentExtension.builder()
						.clock(clock())
						.sourceUserId(fullmakt.fullmektig())
						.sourceUserPrivileges("fullmektig[" + join(",", fullmakt.tema()) + "]")
						.deviceAction("hentdokument_fullmektig")
						.destinationUserId(fullmakt.fullmaktsgiver())
						.hentdokumentRequest(hentdokumentRequest)
						.build())
				.build();
	}
}
