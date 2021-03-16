package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.BrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.B;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode.BID;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode.KTR;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.A;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.D;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.OD;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.U;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.UB;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.I;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.N;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.POL;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.IDENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KTR;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_PEN;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.brukerIdenter;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createDokumentinfoDtoJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoBase;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoWithJournalstatus;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.mockBrukerIdenter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtledTilgangJournalpostServiceTest {

	private final UtledTilgangJournalpostService utledTilgangJournalpostService = new UtledTilgangJournalpostService();

	@BeforeAll
	static void setup() {
		mockBrukerIdenter();
	}

	@Test
	void UtledTilgangHappyPath() {
		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(List.of(createJournalpostDtoBase()), brukerIdenter);

		assertEquals(1, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalstatusMidlertidigAndBrukerNotPart() {
		JournalpostDto journalpostDto = createJournalpostDtoBase();
		journalpostDto.setBruker(BrukerDto.builder().brukerId(ANNEN_PART).build());

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostFerdigstiltAndBrukerNotPart() {
		List<JournalpostDto> journalpostDtoList = new ArrayList<>();

		journalpostDtoList.add(createJournalpostDtoJournalpost(FL, TEMA_PEN, FS22, ANNEN_PART, BID, I));
		journalpostDtoList.add(createJournalpostDtoJournalpost(FS, TEMA_PEN, PEN, ANNEN_PART, BID, I));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(journalpostDtoList, brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostGDPRRestricted() {
		JournalpostDto journalpostDto = createJournalpostDtoBase();
		journalpostDto.setSkjerming(POL);

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalstatusIsNotMidlertidigOrFerdigstilt() {
		List<JournalpostDto> journalpostDtoList = new ArrayList<>();

		journalpostDtoList.add(createJournalpostDtoWithJournalstatus(A));
		journalpostDtoList.add(createJournalpostDtoWithJournalstatus(U));
		journalpostDtoList.add(createJournalpostDtoWithJournalstatus(UB));
		journalpostDtoList.add(createJournalpostDtoWithJournalstatus(OD));
		journalpostDtoList.add(createJournalpostDtoWithJournalstatus(D));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(journalpostDtoList, brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostIsKontrollsak() {
		List<JournalpostDto> journalpostDtoList = new ArrayList<>();

		journalpostDtoList.add(createJournalpostDtoJournalpost(FS, TEMA_KTR, PEN, IDENT, BID, I));
		journalpostDtoList.add(createJournalpostDtoJournalpost(MO, TEMA_PEN, PEN, IDENT, KTR, I));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(journalpostDtoList, brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostIsForvaltningsnotat() {
		JournalpostDto journalpostDto = createJournalpostDtoJournalpost(M, TEMA_PEN, PEN, IDENT, BID, N);
		journalpostDto.setDokumenter(List.of(createDokumentinfoDtoJournalpost(false, FORVALTNINGSNOTAT)));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostIsOrganinternt() {
		JournalpostDto journalpostDto = createJournalpostDtoBase();
		journalpostDto.setDokumenter(List.of(createDokumentinfoDtoJournalpost(true, B)));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangJournalpostService.utledTilgangJournalpost(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}
}

