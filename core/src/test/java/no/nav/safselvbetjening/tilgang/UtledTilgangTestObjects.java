package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.BrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SaksrelasjonDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.mockito.Mockito;

import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.SOK;
import static no.nav.safselvbetjening.domain.Tema.KTR;
import static no.nav.safselvbetjening.domain.Tema.PEN;
import static org.mockito.Mockito.when;

public class UtledTilgangTestObjects {

	static final String IDENT = "12345678911";
	static final String ANNEN_PART = "23456789101";
	static final String AKTOER_ID = "10000000000";
	static final String ANNEN_AKTOER_ID = "12000000000";
	static final String TEMA_KTR = KTR.toString();
	static final String TEMA_PEN = PEN.toString();
	static final List<String> IDENT_LIST = List.of(IDENT, AKTOER_ID);

	static final BrukerIdenter brukerIdenter = Mockito.mock(BrukerIdenter.class);

	static void mockBrukerIdenter() {
		when(brukerIdenter.getIdenter()).thenReturn(IDENT_LIST);
		when(brukerIdenter.getFoedselsnummer()).thenReturn(List.of(IDENT));
	}

	static JournalpostDto createJournalpostDtoDokument(JournalpostTypeCode journalpostTypeCode, String avsenderMottakerId, MottaksKanalCode mottaksKanalCode) {
		return JournalpostDto.builder()
				.journalposttype(journalpostTypeCode)
				.avsenderMottakerId(avsenderMottakerId)
				.mottakskanal(mottaksKanalCode)
				.build();
	}

	static JournalpostDto createJournalpostDtoJournalpost(JournalStatusCode journalStatusCode, String tema, FagsystemCode fagsystemCode,
														  String brukerId, String aktoerId, FagomradeCode fagomradeCode, JournalpostTypeCode journalpostTypeCode) {
		return JournalpostDto.builder()
				.journalstatus(journalStatusCode)
				.fagomrade(fagomradeCode)
				.journalposttype(journalpostTypeCode)
				.saksrelasjon(SaksrelasjonDto.builder()
						.tema(tema)
						.fagsystem(fagsystemCode)
						.aktoerId(aktoerId)
						.build())
				.bruker(BrukerDto.builder()
						.brukerId(brukerId)
						.build())
				.dokumenter(List.of(DokumentInfoDto.builder()
						.organInternt(false)
						.kategori(SOK)
						.build()))
				.build();
	}

	static JournalpostDto.JournalpostDtoBuilder createJournalpostDtoBase() {
		return JournalpostDto.builder()
				.journalstatus(JournalStatusCode.M)
				.fagomrade(FagomradeCode.BID)
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(SaksrelasjonDto.builder()
						.tema(TEMA_PEN)
						.fagsystem(FagsystemCode.PEN)
						.aktoerId(AKTOER_ID)
						.build())
				.bruker(BrukerDto.builder()
						.brukerId(IDENT)
						.build())
				.dokumenter(List.of(DokumentInfoDto.builder()
						.organInternt(false)
						.kategori(SOK)
						.build()));
	}

	static JournalpostDto createJournalpostDtoWithJournalstatus(JournalStatusCode journalStatusCode) {
		return createJournalpostDtoBase().journalstatus(journalStatusCode).build();
	}

	static DokumentInfoDto createDokumentinfoDtoDokument(boolean innskrenketPartsinnsyn, boolean innskrenketTredjepart, boolean kassert, SkjermingTypeCode skjermingTypeCode) {
		return DokumentInfoDto.builder()
				.innskrPartsinnsyn(innskrenketPartsinnsyn)
				.innskrTredjepart(innskrenketTredjepart)
				.varianter(List.of(VariantDto.builder()
						.skjerming(skjermingTypeCode)
						.build()))
				.kassert(kassert)
				.build();
	}
}
