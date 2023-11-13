package no.nav.safselvbetjening.dokumentoversikt.audit;

import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static no.nav.safselvbetjening.MDCUtils.MDC_CALL_ID;
import static no.nav.safselvbetjening.MDCUtils.MDC_CONSUMER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DokumentoversiktAuditTest {
	public static final String DOKUMENTEIER = "11111111111";
	private static final String FULLMEKTIG = "22222222222";
	private static final String FULLMAKTSGIVER = DOKUMENTEIER;

	private final DokumentoversiktAudit audit = new DokumentoversiktAudit(Clock.fixed(Instant.parse("2023-08-11T12:01:01.001Z"), ZoneId.of("Europe/Oslo")));

	@Test
	void shouldMapDokumentoversiktAuditLogAsFullmektig() {
		MDC.put(MDC_CONSUMER_ID, "itest:teamdokumenthandtering:audit");
		MDC.put(MDC_CALL_ID, "1234-5678-9101");

		Fullmakt fullmakt = new Fullmakt(FULLMEKTIG, FULLMAKTSGIVER, List.of("BAR", "FOR"));

		CommonEventFormat commonEventFormat = audit.mapDokumentoversikt(fullmakt);
		assertThat(commonEventFormat.toString())
				.isEqualTo("CEF:0|safselvbetjening|AuditLog|1.0|audit:access|brukers dokumentoversikt hentet av fullmektig|INFO|" +
						   "end=1691755261001 sproc=itest:teamdokumenthandtering:audit devicePayloadId=1234-5678-9101 suid=22222222222 spriv=fullmektig[BAR,FOR] " +
						   "duid=11111111111 act=dokumentoversikt_fullmektig");
	}

	@Test
	void shouldMapOwnDokumentoversiktAuditLog() {
		MDC.put(MDC_CONSUMER_ID, "itest:teamdokumenthandtering:audit");
		MDC.put(MDC_CALL_ID, "1234-5678-9101");

		CommonEventFormat commonEventFormat = audit.mapDokumentoversikt(DOKUMENTEIER);
		assertThat(commonEventFormat.toString())
				.isEqualTo("CEF:0|safselvbetjening|AuditLog|1.0|audit:access|dokumentoversikten til bruker hentet av bruker selv|INFO|" +
						"end=1691755261001 sproc=itest:teamdokumenthandtering:audit devicePayloadId=1234-5678-9101 suid=11111111111 spriv=null " +
						"duid=11111111111 act=dokumentoversikt_bruker");
	}
}