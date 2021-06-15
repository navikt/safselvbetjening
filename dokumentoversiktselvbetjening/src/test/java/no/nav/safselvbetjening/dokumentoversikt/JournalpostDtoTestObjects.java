package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.AvsenderMottakerIdTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.BrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.LogiskVedleggDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SaksrelasjonDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.TilleggsopplysningDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.UtsendingsKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse;
import no.nav.safselvbetjening.service.BrukerIdenter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JournalpostDtoTestObjects {

	static final Long JOURNALPOST_ID = 40000000L;
	static final Long DOKUMENT_INFO_ID = 50000000L;
	static final String IDENT = "12345678911";
	static final String AKTOER_ID = "10000000000";
	static final VariantFormatCode VARIANT_FORMAT_CODE_ARKIV = VariantFormatCode.ARKIV;
	static final VariantFormatCode VARIANT_FORMAT_CODE_SLADDET = VariantFormatCode.SLADDET;
	static final SkjermingTypeCode SKJERMING_TYPE_CODE_POL = SkjermingTypeCode.POL;
	static final String BREVKODE = "brevkodeX";
	static final String INNHOLD = "Søknad om foreldrepenger";
	static final LocalDateTime DATO_OPPRETTET_LDT = LocalDateTime.of(2020, 1, 1, 0, 0);
	static final Date DATO_OPPRETTET = Date.from(DATO_OPPRETTET_LDT.atZone(ZoneId.systemDefault()).toInstant());
	static final Date AVS_RETUR_DATO = new Date(2000L);
	static final Date SENDT_PRINT_DATO = new Date(3000L);
	static final Date EKSPEDERT_DATO = new Date(4000L);
	static final Date DOKUMENT_DATO = new Date(5000L);
	static final LocalDateTime JOURNAL_DATO_LDT = LocalDateTime.of(2021, 1, 1, 0, 0);
	static final Date JOURNAL_DATO = Date.from(JOURNAL_DATO_LDT.atZone(ZoneId.systemDefault()).toInstant());
	;
	static final Date MOTTAT_DATO = new Date(7000L);
	static final Date DATO_FERDIGSTILT = new Date(8000L);
	static final String SAKS_ID = "12345";
	static final FagsystemCode ARKIVSAKSYSTEM_GOSYS = FagsystemCode.FS22;
	static final FagsystemCode ARKIVSAKSYSTEM_PENSJON = FagsystemCode.PEN;
	static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE_CODE = AvsenderMottakerIdTypeCode.FNR;
	static final FagomradeCode FAGOMRADE = FagomradeCode.FOR;
	static final String JOURNALFOERT_AV = "Automatisk jobb";
	static final String BEHANDLINGSTEMA = "ab0072";
	static final String BEHANDLINGSTEMANAVN = "Foreldrepenger ved adopsjon";
	static final String AVSENDER_MOTTAKER_NAVN = "Bjarne Betjent";
	static final String AVSENDER_MOTTAKER_LAND = "NO";
	static final String JOURNALFOERENDE_ENHET = "2990";
	static final String OPPRETTET_AV_NAVN = "Max Mekker";
	static final String TILLEGGSOPPLYSNING_NOKKEL = "bucid";
	static final String TILLEGGSOPPLYSNING_VERDI = "21521";
	static final String FILNAVN_1 = "filnavn1";
	static final String FILNAVN_2 = "filnavn2";
	static final String AVSENDER_MOTTAKER_ID = "00000000000";
	static final String LOGISK_VEDLEGG_ID = "logisk1";
	static final String LOGISK_VEDLEGG_TITTEL = "logisktittel";
	static final String DOKUMENTTYPE_ID = "00000001";
	static final String FILUUID_1 = "abcd";
	static final String FILUUID_2 = "dcba";
	static final String BRUKER_ID_PERSON = "11111111111";
	static final String ANTALL_RETUR = "3";
	static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";
	static final String TITTEL = "Søknad om foreldrepenger ved adopsjon";
	static final DokumentKategoriCode KATEGORI = DokumentKategoriCode.ES;
	static final String FILSTORRELSE_1 = "100";
	static final String FILSTORRELSE_2 = "200";
	static final String FILTYPE = "PDF";

	static JournalpostDto buildJournalpostDtoUtgaaendeType(JournalStatusCode journalStatusCode) {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.U)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, ARKIVSAKSYSTEM_GOSYS, AKTOER_ID, FAGOMRADE.name(),
						null, null, null, null, null))
				.utsendingskanal(UtsendingsKanalCode.SDP)
				.journalstatus(journalStatusCode)
				.journalDato(JOURNAL_DATO)
				.dokumentDato(DOKUMENT_DATO)
				.avsReturDato(AVS_RETUR_DATO)
				.sendtPrintDato(SENDT_PRINT_DATO)
				.ekspedertDato(EKSPEDERT_DATO)
				.antallRetur(ANTALL_RETUR)
				.build();
	}

	static JournalpostDto buildJournalpostDtoNotatType(JournalStatusCode journalStatusCode) {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, ARKIVSAKSYSTEM_GOSYS, AKTOER_ID, FAGOMRADE.name(),
						null, null, null, null, null))
				.utsendingskanal(UtsendingsKanalCode.SDP)
				.journalstatus(journalStatusCode)
				.journalDato(JOURNAL_DATO)
				.dokumentDato(DOKUMENT_DATO)
				.avsReturDato(AVS_RETUR_DATO)
				.sendtPrintDato(SENDT_PRINT_DATO)
				.ekspedertDato(EKSPEDERT_DATO)
				.antallRetur(ANTALL_RETUR)
				.build();
	}

	static JournalpostDto buildJournalpostDtoInngaaendeType() {
		return baseJournalpostDto()
				.bruker(BrukerDto.builder().brukerId(BRUKER_ID_PERSON).brukerIdType("PERSON").build())
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, ARKIVSAKSYSTEM_GOSYS, AKTOER_ID, FAGOMRADE.name(),
						null, null, null, null, null))
				.mottattDato(MOTTAT_DATO)
				.journalDato(JOURNAL_DATO)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.build();
	}

	static JournalpostDto buildJournalpostDtoMottatt() {
		return baseJournalpostDto()
				.journalstatus(JournalStatusCode.M)
				.bruker(null)
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(null, null, null, null, null,
						null, null, null, null, null))
				.mottattDato(MOTTAT_DATO)
				.mottakskanal(MottaksKanalCode.SKAN_IM)
				.build();
	}

	static JournalpostDto.JournalpostDtoBuilder baseJournalpostDto() {
		return JournalpostDto.builder()
				.journalpostId(JOURNALPOST_ID)
				.nextJournalpostId(405252858L)
				.innhold(INNHOLD)
				.fagomrade(FAGOMRADE)
				.behandlingstema(BEHANDLINGSTEMA)
				.behandlingstemanavn(BEHANDLINGSTEMANAVN)
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottakerIdType(AVSENDER_MOTTAKER_ID_TYPE_CODE)
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerLand(AVSENDER_MOTTAKER_LAND)
				.journalforendeEnhet(JOURNALFOERENDE_ENHET)
				.journalfortAvNavn(JOURNALFOERT_AV)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.datoOpprettet(DATO_OPPRETTET)
				.journalstatus(JournalStatusCode.J)
				.skjerming(SKJERMING_TYPE_CODE_POL)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.tilleggsopplysninger(Collections.singletonList(TilleggsopplysningDto.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.dokumenter(buildDokument());
	}

	private static DokumentInfoDto.DokumentInfoDtoBuilder baseDokumentInfoDto() {
		return DokumentInfoDto.builder()
				.dokumentInfoId(DOKUMENT_INFO_ID.toString())
				.tittel(TITTEL)
				.brevkode(BREVKODE)
				.dokumenttypeId(DOKUMENTTYPE_ID)
				.datoFerdigstilt(DATO_FERDIGSTILT)
				.origJournalpostId(JOURNALPOST_ID)
				.skjerming(SKJERMING_TYPE_CODE_POL)
				.kategori(KATEGORI)
				.logiske(logiskeVedlegg());
	}

	private static List<DokumentInfoDto> buildDokument() {
		return Collections.singletonList(baseDokumentInfoDto()
				.varianter(Arrays.asList(VariantDto.builder()
								.skjerming(SKJERMING_TYPE_CODE_POL)
								.variantf(VARIANT_FORMAT_CODE_ARKIV)
								.filnavn(FILNAVN_1)
								.filuuid(FILUUID_1)
								.filtype(FILTYPE)
								.filstorrelse(FILSTORRELSE_1)
								.build(),
						VariantDto.builder()
								.skjerming(null)
								.variantf(VARIANT_FORMAT_CODE_SLADDET)
								.filnavn(FILNAVN_2)
								.filtype(FILTYPE)
								.filuuid(FILUUID_2)
								.filstorrelse(FILSTORRELSE_2)
								.build()))
				.build());
	}

	static List<DokumentInfoDto> buildDokumentWithVarianter(String... varianter) {
		return Collections.singletonList(baseDokumentInfoDto()
				.varianter(Stream.of(varianter).map(variant -> VariantDto.builder()
						.variantf(VariantFormatCode.valueOf(variant))
						.build()).collect(Collectors.toList()))
				.build());
	}

	private static List<LogiskVedleggDto> logiskeVedlegg() {
		LogiskVedleggDto logiskVedleggDto = new LogiskVedleggDto();
		logiskVedleggDto.setVedleggId(LOGISK_VEDLEGG_ID);
		logiskVedleggDto.setTittel(LOGISK_VEDLEGG_TITTEL);
		return Collections.singletonList(logiskVedleggDto);
	}

	static BrukerIdenter createBrukerIdenter() {
		List<PdlResponse.PdlIdent> pdlIdenter = new ArrayList<>();
		pdlIdenter.add(createPdlIdent(IDENT, false, PdlResponse.PdlGruppe.FOLKEREGISTERIDENT));
		pdlIdenter.add(createPdlIdent(AKTOER_ID, false, PdlResponse.PdlGruppe.AKTORID));
		return new BrukerIdenter(pdlIdenter);
	}

	private static PdlResponse.PdlIdent createPdlIdent(String ident, boolean historisk, PdlResponse.PdlGruppe gruppe) {
		PdlResponse.PdlIdent pdlIdent = new PdlResponse.PdlIdent();
		pdlIdent.setIdent(ident);
		pdlIdent.setHistorisk(historisk);
		pdlIdent.setGruppe(gruppe);
		return pdlIdent;
	}
}
