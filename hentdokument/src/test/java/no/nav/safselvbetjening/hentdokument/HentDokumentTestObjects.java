package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.InnsynCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.SkjermingTypeCode;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class HentDokumentTestObjects {

	static final String IDENT = "12345678911";
	static final String AVSENDER_MOTTAKER_ID = "11987654321";
	static final String AKTOER_ID = "32345678911";
	static final String JOURNALPOST_ID = "40000000";
	static final String ARKIVSAKSYSTEM_GOSYS = "FS22";
	static final String ARKIVSAKSYSTEM_PENSJON = "PEN";
	static final String TEMA = "FAR";
	static final String TEMA_PENSJON_UFO = "UFO";
	static final String FORVALTNINGSNOTAT = "FORVALTNINGSNOTAT";
	static final LocalDateTime DATO_OPPRETTET = LocalDate.of(2018, Month.FEBRUARY, 23).atStartOfDay();
	static final LocalDateTime DATO_JOURNALFOERT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

	public static ArkivJournalpost.ArkivJournalpostBuilder baseArkivJournalpost() {
		return ArkivJournalpost.builder()
				.journalpostId(Long.valueOf(JOURNALPOST_ID))
				.avsenderMottaker(new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, null, null))
				.type(JournalpostTypeCode.I.name())
				.status(JournalStatusCode.M.name())
				.mottakskanal(MottaksKanalCode.NAV_NO.name())
				.relevanteDatoer(new ArkivRelevanteDatoer(DATO_OPPRETTET, DATO_JOURNALFOERT))
				.fagomraade(FagomradeCode.PEN.name());
	}

	public static ArkivJournalpost arkivJournalpost() {
		return baseArkivJournalpost()
				.skjerming(SkjermingTypeCode.POL.name())
				.bruker(new ArkivBruker(IDENT, null))
				.innsyn(InnsynCode.BRUK_STANDARDREGLER.name())
				.saksrelasjon(ArkivSaksrelasjon.builder()
						.sakId(1L)
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(true)
						.sak(new ArkivSak(TEMA, AKTOER_ID, null, null, null))
						.build())
				.dokumenter(List.of(arkivDokumentinfo(VariantFormatCode.ARKIV.name()), arkivDokumentinfo(VariantFormatCode.ORIGINAL.name())))
				.build();
	}

	public static ArkivJournalpost pensjonArkivJournalpost() {
		return baseArkivJournalpost()
				.skjerming(SkjermingTypeCode.POL.name())
				.bruker(new ArkivBruker(IDENT, null))
				.innsyn(InnsynCode.BRUK_STANDARDREGLER.name())
				.saksrelasjon(ArkivSaksrelasjon.builder()
						.sakId(1L)
						.fagsystem(ARKIVSAKSYSTEM_PENSJON)
						.feilregistrert(true)
						.sak(null)
						.build())
				.dokumenter(List.of(arkivDokumentinfo(VariantFormatCode.ARKIV.name()), arkivDokumentinfo(VariantFormatCode.ORIGINAL.name())))
				.build();
	}

	public static ArkivDokumentinfo arkivDokumentinfo(String variantFormat) {
		return new ArkivDokumentinfo(40000000L, SkjermingTypeCode.FEIL.name(), FORVALTNINGSNOTAT, null,
				List.of(new ArkivFildetaljer(SkjermingTypeCode.FEIL.name(), variantFormat)));
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
