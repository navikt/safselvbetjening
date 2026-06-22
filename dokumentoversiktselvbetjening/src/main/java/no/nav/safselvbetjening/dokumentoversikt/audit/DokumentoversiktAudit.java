package no.nav.safselvbetjening.dokumentoversikt.audit;

import no.nav.safselvbetjening.audit.Audit;
import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.representasjon.Fullmakt;
import no.nav.safselvbetjening.representasjon.Representasjonsforhold;
import no.nav.safselvbetjening.representasjon.Vergemaal;

import java.time.Clock;

import static java.lang.String.join;
import static no.nav.safselvbetjening.audit.cef.Headers.DOKUMENTOVERSIKT_EGEN_HEADERS;
import static no.nav.safselvbetjening.audit.cef.Headers.DOKUMENTOVERSIKT_FULLMAKT_HEADERS;
import static no.nav.safselvbetjening.audit.cef.Headers.DOKUMENTOVERSIKT_VERGE_HEADERS;

public record DokumentoversiktAudit(Clock clock) implements Audit {

	public void logSomRepresentant(Representasjonsforhold representasjon) {
		switch (representasjon) {
			case Fullmakt fullmakt -> log(mapDokumentoversikt(fullmakt));
			case Vergemaal vergemaal -> log(mapDokumentoversikt(vergemaal));
		}
	}

	public void logSomBruker(String ident) {
		log(mapDokumentoversikt(ident));
	}

	CommonEventFormat mapDokumentoversikt(Fullmakt fullmakt) {
		return CommonEventFormat.builder()
				.headers(DOKUMENTOVERSIKT_FULLMAKT_HEADERS)
				.extension(DokumentoversiktExtension.builder()
						.clock(clock())
						.sourceUserId(fullmakt.fullmektig())
						.sourceUserPrivileges("fullmektig[" + join(",", fullmakt.tema()) + "]")
						.deviceAction("dokumentoversikt_fullmektig")
						.destinationUserId(fullmakt.fullmaktsgiver())
						.build())
				.build();
	}

	CommonEventFormat mapDokumentoversikt(Vergemaal vergemaal) {
		return CommonEventFormat.builder()
				.headers(DOKUMENTOVERSIKT_VERGE_HEADERS)
				.extension(DokumentoversiktExtension.builder()
						.clock(clock())
						.sourceUserId(vergemaal.verge())
						.sourceUserPrivileges("verge[" + join(",", vergemaal.tema()) + "]")
						.deviceAction("dokumentoversikt_vergemaal")
						.destinationUserId(vergemaal.vergehaver())
						.build())
				.build();
	}

	CommonEventFormat mapDokumentoversikt(String ident) {
		return CommonEventFormat.builder()
				.headers(DOKUMENTOVERSIKT_EGEN_HEADERS)
				.extension(DokumentoversiktExtension.builder()
						.clock(clock())
						.sourceUserId(ident)
						.deviceAction("dokumentoversikt_bruker")
						.destinationUserId(ident)
						.build())
				.build();
	}
}
