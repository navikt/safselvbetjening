package no.nav.safselvbetjening.dokumentoversikt.audit;

import no.nav.safselvbetjening.MDCUtils;
import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DokumentoversiktAuditTest {
	private final String FULLMEKTIG = "22222222222";
	private final String FULLMAKTSGIVER = "11111111111";

	private final DokumentoversiktAudit audit = new DokumentoversiktAudit(Clock.fixed(Instant.parse("2023-08-11T12:01:01.001Z"), ZoneId.of("Europe/Oslo")));

	@Test
	void shouldMapDokumentoversiktAuditLog() {
		MDC.put(MDCUtils.MDC_CONSUMER_ID, "itest:teamdokumenthandtering:audit");
		MDC.put(MDCUtils.MDC_CALL_ID, "1234-5678-9101");

		Fullmakt fullmakt = new Fullmakt(FULLMEKTIG, FULLMAKTSGIVER, List.of("BAR", "FOR"));

		CommonEventFormat commonEventFormat = audit.mapDokumentoversikt(fullmakt);
		assertThat(commonEventFormat.toString())
				.isEqualTo("CEF:0|safselvbetjening|AuditLog|1.0|audit:access|brukers dokumentoversikt hentet av fullmektig|INFO|" +
						   "end=1691755261001 sproc=itest:teamdokumenthandtering:audit spid=1234-5678-9101 suid=22222222222 spriv=fullmektig[BAR,FOR] " +
						   "duid=11111111111 act=dokumentoversikt_fullmektig");
	}
}