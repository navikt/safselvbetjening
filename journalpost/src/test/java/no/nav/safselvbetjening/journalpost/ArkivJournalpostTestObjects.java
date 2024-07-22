package no.nav.safselvbetjening.journalpost;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.InnsynCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.UtsendingsKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.VariantFormatCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivAvsenderMottaker;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivBruker;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivDokumentinfo;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivFildetaljer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivRelevanteDatoer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSak;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSaksrelasjon;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse;
import no.nav.safselvbetjening.service.BrukerIdenter;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostMapper.TILKNYTTET_SOM_HOVEDDOKUMENT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostMapper.TILKNYTTET_SOM_VEDLEGG;

final class ArkivJournalpostTestObjects {
	static final String JOURNALPOST_ID = "40000000";
	static final String TEMA = FagomradeCode.HJE.name();
	static final String INNHOLD = "Søknad om hjelpemidler";
	static final String KANAL_REFERANSE_ID = "11111111-2222-3333-4444-555555555555";
	static final String FAGSAKNR = "9000";
	static final String APPLIKASJON = "HJELPEMIDLER";
	static final String AVSENDER_MOTTAKER_ID = "11987654321";
	static final String AVSENDER_MOTTAKER_ID_TYPE = "FNR";
	static final String AVSENDER_MOTTAKER_NAVN = "RHAENYRA TARGARYEN";
	static final String BRUKER_IDENT = "12345678911";
	static final String ARKIVSAK_AKTOER_ID = "32345678911";
	static final String ARKIVSAKSYSTEM_GOSYS = "FS22";
	static final String KATEGORI_FORVALTNINGSNOTAT = "FORVALTNINGSNOTAT";
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_OPPRETTET = LocalDate.of(2018, Month.FEBRUARY, 23).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_JOURNALFOERT = LocalDate.of(2019, Month.JANUARY, 18).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_EKSPEDERT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_MOTTATT = LocalDate.of(2021, Month.JANUARY, 18).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_DOKUMENT = LocalDate.of(2022, Month.JANUARY, 18).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_RETUR = LocalDate.of(2024, Month.JANUARY, 18).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final OffsetDateTime ARKIVJOURNALPOST_DATO_SENDT_PRINT = LocalDate.of(2025, Month.JANUARY, 18).atStartOfDay().atOffset(ZoneOffset.of("+00:00"));
	static final Long ARKIVSAK_ID = 140000000L;
	static final long HOVEDDOKUMENT_DOKUMENT_INFO_ID = 41000000L;
	static final String HOVEDDOKUMENT_BREVKODE = "NAV 10-07.53";
	static final String HOVEDDOKUMENT_TITTEL = "Søknad om hjelpemidler";
	static final String HOVEDDOKUMENT_FIL_UUID = "11111111-2222-3333-4444-000000000001";
	static final String HOVEDDOKUMENT_FILTYPE = "PDF";
	static final String HOVEDDOKUMENT_FIL_STOERRELSE = "1024";
	static final long VEDLEGG_DOKUMENT_INFO_ID = 42000000L;
	static final String VEDLEGG_FIL_UUID = "11111111-2222-3333-4444-000000000002";
	static final String VEDLEGG_TITTEL = "Kvitteringsside for dokumentinnsending";
	static final String VEDLEGG_BREVKODE = "L7";
	static final String VEDLEGG_FIL_STOERRELSE = "2048";
	static final String VEDLEGG_FIL_TYPE = "PDFA";
	// pensjon spesifikt
	static final String ARKIVSAKSYSTEM_PENSJON = "PEN";
	static final Long PENSJON_FAGSAKID = 2000000L;
	static final String TEMA_PENSJON_ALDERSPENSJON = FagomradeCode.PEN.name();
	static final String TEMA_PENSJON_UFORETRYGD = FagomradeCode.UFO.name();

	static ArkivJournalpost.ArkivJournalpostBuilder baseArkivJournalpost() {
		return ArkivJournalpost.builder()
				.journalpostId(Long.valueOf(JOURNALPOST_ID))
				.avsenderMottaker(new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_ID_TYPE, AVSENDER_MOTTAKER_NAVN))
				.type(JournalpostTypeCode.I.name())
				.status(JournalStatusCode.M.name())
				.mottakskanal(MottaksKanalCode.NAV_NO.name())
				.relevanteDatoer(new ArkivRelevanteDatoer(ARKIVJOURNALPOST_DATO_OPPRETTET, ARKIVJOURNALPOST_DATO_JOURNALFOERT, ARKIVJOURNALPOST_DATO_EKSPEDERT, ARKIVJOURNALPOST_DATO_MOTTATT, ARKIVJOURNALPOST_DATO_DOKUMENT, ARKIVJOURNALPOST_DATO_RETUR, ARKIVJOURNALPOST_DATO_SENDT_PRINT))
				.fagomraade(FagomradeCode.HJE.name())
				.innhold(INNHOLD)
				.kanalreferanseId(KANAL_REFERANSE_ID);
	}

	static ArkivJournalpost inngaaendeArkivJournalpost() {
		return baseArkivJournalpost()
				.skjerming(SkjermingTypeCode.POL.name())
				.bruker(new ArkivBruker(BRUKER_IDENT, null))
				.innsyn(InnsynCode.BRUK_STANDARDREGLER.name())
				.saksrelasjon(ArkivSaksrelasjon.builder()
						.sakId(ARKIVSAK_ID)
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(true)
						.sak(new ArkivSak(TEMA, ARKIVSAK_AKTOER_ID, null, FAGSAKNR, APPLIKASJON))
						.build())
				.dokumenter(List.of(hoveddokumentArkivDokumentinfo(), vedleggArkivDokumentinfo()))
				.build();
	}

	static ArkivJournalpost utgaaendeArkivJournalpost() {
		return baseArkivJournalpost()
				.type(JournalpostTypeCode.U.name())
				.mottakskanal(null)
				.status(E.name())
				.utsendingskanal(UtsendingsKanalCode.SDP.name())
				.skjerming(SkjermingTypeCode.POL.name())
				.bruker(new ArkivBruker(BRUKER_IDENT, null))
				.innsyn(InnsynCode.BRUK_STANDARDREGLER.name())
				.saksrelasjon(ArkivSaksrelasjon.builder()
						.sakId(ARKIVSAK_ID)
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(true)
						.sak(new ArkivSak(TEMA, ARKIVSAK_AKTOER_ID, null, FAGSAKNR, APPLIKASJON))
						.build())
				.dokumenter(List.of(hoveddokumentArkivDokumentinfo(), vedleggArkivDokumentinfo()))
				.build();
	}

	static ArkivJournalpost pensjonArkivJournalpost() {
		return baseArkivJournalpost()
				.fagomraade(TEMA_PENSJON_ALDERSPENSJON)
				.skjerming(SkjermingTypeCode.POL.name())
				.bruker(new ArkivBruker(BRUKER_IDENT, null))
				.innsyn(InnsynCode.BRUK_STANDARDREGLER.name())
				.saksrelasjon(ArkivSaksrelasjon.builder()
						.sakId(PENSJON_FAGSAKID)
						.fagsystem(ARKIVSAKSYSTEM_PENSJON)
						.feilregistrert(true)
						.sak(null)
						.build())
				.dokumenter(List.of(hoveddokumentArkivDokumentinfo(), vedleggArkivDokumentinfo()))
				.build();
	}

	static ArkivDokumentinfo hoveddokumentArkivDokumentinfo() {
		return new ArkivDokumentinfo(HOVEDDOKUMENT_DOKUMENT_INFO_ID, TILKNYTTET_SOM_HOVEDDOKUMENT, SkjermingTypeCode.FEIL.name(), KATEGORI_FORVALTNINGSNOTAT, false,
				List.of(new ArkivFildetaljer(SkjermingTypeCode.FEIL.name(), VariantFormatCode.ARKIV.name(), HOVEDDOKUMENT_FIL_STOERRELSE, HOVEDDOKUMENT_FILTYPE, HOVEDDOKUMENT_FIL_UUID)), HOVEDDOKUMENT_TITTEL, HOVEDDOKUMENT_BREVKODE, true);
	}

	static ArkivDokumentinfo vedleggArkivDokumentinfo() {
		return new ArkivDokumentinfo(VEDLEGG_DOKUMENT_INFO_ID, TILKNYTTET_SOM_VEDLEGG, SkjermingTypeCode.FEIL.name(), KATEGORI_FORVALTNINGSNOTAT, null,
				List.of(new ArkivFildetaljer(SkjermingTypeCode.FEIL.name(), VariantFormatCode.ARKIV.name(), VEDLEGG_FIL_STOERRELSE, VEDLEGG_FIL_TYPE, VEDLEGG_FIL_UUID)), VEDLEGG_TITTEL, VEDLEGG_BREVKODE, null);
	}


	static BrukerIdenter createBrukerIdenter() {
		List<PdlResponse.PdlIdent> pdlIdenter = new ArrayList<>();
		pdlIdenter.add(createPdlIdent(BRUKER_IDENT, false, PdlResponse.PdlGruppe.FOLKEREGISTERIDENT));
		pdlIdenter.add(createPdlIdent(ARKIVSAK_AKTOER_ID, false, PdlResponse.PdlGruppe.AKTORID));
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
