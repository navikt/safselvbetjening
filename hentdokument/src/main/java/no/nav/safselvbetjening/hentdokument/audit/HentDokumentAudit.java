package no.nav.safselvbetjening.hentdokument.audit;

import no.nav.safselvbetjening.audit.Audit;
import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;
import no.nav.safselvbetjening.representasjon.Fullmakt;
import no.nav.safselvbetjening.representasjon.Representasjonsforhold;
import no.nav.safselvbetjening.representasjon.Vergemaal;

import java.time.Clock;

import static java.lang.String.join;
import static no.nav.safselvbetjening.audit.cef.Headers.HENT_DOKUMENT_EGEN_HEADERS;
import static no.nav.safselvbetjening.audit.cef.Headers.HENT_DOKUMENT_FULLMAKT_HEADERS;
import static no.nav.safselvbetjening.audit.cef.Headers.HENT_DOKUMENT_VERGE_HEADERS;

public record HentDokumentAudit(Clock clock) implements Audit {

	public void logSomRepresentant(Representasjonsforhold representasjonsforhold, HentdokumentRequest hentdokumentRequest) {
		switch (representasjonsforhold) {
			case Fullmakt fullmakt -> log(mapHentDokument(fullmakt, hentdokumentRequest));
			case Vergemaal vergemaal -> log(mapHentDokument(vergemaal, hentdokumentRequest));
		}
	}

	public void logSomBruker(HentdokumentRequest hentdokumentRequest, String ident) {
		log(mapHentDokument(hentdokumentRequest, ident));
	}

	CommonEventFormat mapHentDokument(Fullmakt fullmakt, HentdokumentRequest hentdokumentRequest) {
		return CommonEventFormat.builder()
				.headers(HENT_DOKUMENT_FULLMAKT_HEADERS)
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

	CommonEventFormat mapHentDokument(Vergemaal vergemaal, HentdokumentRequest hentdokumentRequest) {
		return CommonEventFormat.builder()
				.headers(HENT_DOKUMENT_VERGE_HEADERS)
				.extension(HentDokumentExtension.builder()
						.clock(clock())
						.sourceUserId(vergemaal.verge())
						.sourceUserPrivileges("verge[" + join(",", vergemaal.tema()) + "]")
						.deviceAction("hentdokument_vergemaal")
						.destinationUserId(vergemaal.vergehaver())
						.hentdokumentRequest(hentdokumentRequest)
						.build())
				.build();
	}

	CommonEventFormat mapHentDokument(HentdokumentRequest hentdokumentRequest, String ident) {
		return CommonEventFormat.builder()
				.headers(HENT_DOKUMENT_EGEN_HEADERS)
				.extension(HentDokumentExtension.builder()
						.clock(clock())
						.sourceUserId(ident)
						.deviceAction("hentdokument_bruker")
						.destinationUserId(ident)
						.hentdokumentRequest(hentdokumentRequest)
						.build())
				.build();
	}
}
