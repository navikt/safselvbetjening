package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.InnsynCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost.TilgangBrukerDto;
import no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost.TilgangDokumentInfoDto;
import no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost.TilgangSakDto;
import no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost.TilgangVariantDto;
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

	public static TilgangJournalpostDto.TilgangJournalpostDtoBuilder createTilgangJournalpostDto() {
		return createBaseTilgangJournalpost()
				.dokument(createDokumentInfoDto().build())
				.skjerming(SkjermingTypeCode.POL)
				.bruker(TilgangBrukerDto.builder().brukerId(IDENT).build())
				.innsyn(InnsynCode.BRUK_STANDARDREGLER)
				.sak(TilgangSakDto.builder()
						.aktoerId(AKTOER_ID)
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
						.feilregistrert(true)
						.tema(TEMA)
						.build());
	}

	public static TilgangJournalpostDto.TilgangJournalpostDtoBuilder createBaseTilgangJournalpost() {
		return TilgangJournalpostDto.builder()
				.journalpostId(JOURNALPOST_ID)
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.journalpostType(JournalpostTypeCode.I)
				.journalStatus(JournalStatusCode.M)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.datoOpprettet(DATO_OPPRETTET)
				.journalfoertDato(DATO_JOURNALFOERT)
				.fagomrade(FagomradeCode.PEN);
	}

	public static TilgangDokumentInfoDto.TilgangDokumentInfoDtoBuilder createDokumentInfoDto() {
		return TilgangDokumentInfoDto
				.builder()
				.kassert(false)
				.kategori(FORVALTNINGSNOTAT)
				.variant(TilgangVariantDto.builder()
						.skjerming(SkjermingTypeCode.FEIL)
						.build());
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
