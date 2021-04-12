package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangBrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangDokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangSakDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangVariantDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;

public class HentDokumentTestObjects {

	static final String IDENT = "12345678911";
	static final String AVSENDER_MOTTAKER_ID = "11987654321";
	static final String AKTOER_ID = "32345678911";
	static final String JOURNALPOST_ID = "40000000";
	static final String FAGSYSTEM = "PEN";
	static final String TEMA = "FAR";
	static final LocalDateTime DATO_OPPRETTET = LocalDate.of(2018, Month.FEBRUARY, 23).atStartOfDay();
	static final LocalDateTime DATO_JOURNALFOERT = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();

	public static TilgangJournalpostDto.TilgangJournalpostDtoBuilder createTilgangJournalpostDto() {
		return createBaseTilgangJournalpost()
				.dokument(createDokumentInfoDto().build())
				.skjerming(SkjermingTypeCode.POL)
				.bruker(TilgangBrukerDto.builder().brukerId(IDENT).build())
				.sak(TilgangSakDto.builder()
						.aktoerId(AKTOER_ID)
						.fagsystem(FAGSYSTEM)
						.feilregistrert(true)
						.tema(TEMA)
						.build());
	}

	public static TilgangJournalpostDto.TilgangJournalpostDtoBuilder createBaseTilgangJournalpost(){
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
				.organinternt(null)
				.innskrenketPartsinnsyn(true)
				.innskrenketTredjepart(true)
				.variant(TilgangVariantDto.builder()
						.skjerming(SkjermingTypeCode.FEIL)
						.build());
	}
}
