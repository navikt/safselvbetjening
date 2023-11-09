package no.nav.safselvbetjening.hentdokument.audit;

import no.nav.safselvbetjening.MDCUtils;
import no.nav.safselvbetjening.audit.cef.CommonEventFormat;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HentDokumentAuditTest {
	private static final String JOURNALPOST_ID = "1000";
	private static final String DOKUMENT_INFO_ID = "1001";
	private static final String VARIANT_FORMAT = "ARKIV";
	private final String FULLMEKTIG = "22222222222";
	private final String FULLMAKTSGIVER = "11111111111";

	private final HentDokumentAudit audit = new HentDokumentAudit(Clock.fixed(Instant.parse("2023-08-11T12:01:01.001Z"), ZoneId.of("Europe/Oslo")));

	@Test
	void shouldMapFullmaktHentDokumentAuditLog() {
		MDC.put(MDCUtils.MDC_CONSUMER_ID, "itest:teamdokumenthandtering:audit");
		MDC.put(MDCUtils.MDC_CALL_ID, "1234-5678-9101");

		Fullmakt fullmakt = new Fullmakt(FULLMEKTIG, FULLMAKTSGIVER, List.of("BAR", "FOR"));
		HentdokumentRequest hentdokumentRequest = HentdokumentRequest.builder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.variantFormat(VARIANT_FORMAT)
				.build();

		CommonEventFormat commonEventFormat = audit.mapHentDokument(fullmakt, hentdokumentRequest);
		assertThat(commonEventFormat.toString())
				.isEqualTo("CEF:0|safselvbetjening|AuditLog|1.0|audit:access|brukers dokument hentet av fullmektig|INFO|" +
						   "end=1691755261001 sproc=itest:teamdokumenthandtering:audit devicePayloadId=1234-5678-9101 suid=22222222222 spriv=fullmektig[BAR,FOR] " +
						   "duid=11111111111 act=hentdokument_fullmektig flexString1=1000 flexString1Label=journalpostId flexString2=1001 flexString2Label=dokumentInfoId cs3=ARKIV cs3Label=variantFormat");
	}

	@Test
	void shouldMapOwnHentDokumentAuditLog() {
		MDC.put(MDCUtils.MDC_CONSUMER_ID, "itest:teamdokumenthandtering:audit");
		MDC.put(MDCUtils.MDC_CALL_ID, "1234-5678-9101");

		String ident = FULLMAKTSGIVER;
		HentdokumentRequest hentdokumentRequest = HentdokumentRequest.builder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.variantFormat(VARIANT_FORMAT)
				.build();

		CommonEventFormat commonEventFormat = audit.mapHentDokument(hentdokumentRequest, ident);
		assertThat(commonEventFormat.toString())
				.isEqualTo("CEF:0|safselvbetjening|AuditLog|1.0|audit:access|brukers dokument hentet av bruker selv|INFO|" +
						"end=1691755261001 sproc=itest:teamdokumenthandtering:audit devicePayloadId=1234-5678-9101 suid=11111111111 spriv=null " +
						"duid=11111111111 act=hentdokument_bruker flexString1=1000 flexString1Label=journalpostId flexString2=1001 flexString2Label=dokumentInfoId cs3=ARKIV cs3Label=variantFormat");
	}
}