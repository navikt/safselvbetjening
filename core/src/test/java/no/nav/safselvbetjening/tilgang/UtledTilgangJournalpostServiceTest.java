package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.BrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.IDENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KTR;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_PEN;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.brukerIdenter;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoBase;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoWithJournalstatus;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.mockBrukerIdenter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtledTilgangJournalpostServiceTest {
/*
	private final UtledTilgangDokumentoversiktService utledTilgangDokumentoversiktService;

	UtledTilgangJournalpostServiceTest() {
		SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();
		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));
		utledTilgangDokumentoversiktService = new UtledTilgangDokumentoversiktService(safSelvbetjeningProperties);
	}

	@BeforeAll
	static void setup() {
		mockBrukerIdenter();
	}

	@Test
	void UtledTilgangHappyPath() {
		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(List.of(createJournalpostDtoBase().build()), brukerIdenter);

		assertEquals(1, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalstatusMidlertidigAndBrukerNotPart() {
		JournalpostDto journalpostDto = createJournalpostDtoBase().bruker(BrukerDto.builder().brukerId(ANNEN_PART).build()).build();

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostFerdigstiltAndBrukerNotPart() {
		List<JournalpostDto> journalpostDtoList = new ArrayList<>();

		journalpostDtoList.add(createJournalpostDtoJournalpost(FL, TEMA_PEN, FS22, ANNEN_PART, ANNEN_AKTOER_ID, BID, I));
		journalpostDtoList.add(createJournalpostDtoJournalpost(FS, TEMA_PEN, PEN, ANNEN_PART, AKTOER_ID, BID, I));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(journalpostDtoList, brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostGDPRRestricted() {
		JournalpostDto journalpostDto = createJournalpostDtoBase().skjerming(POL).build();

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(List.of(journalpostDto), brukerIdenter);

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

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(journalpostDtoList, brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostIsKontrollsak() {
		List<JournalpostDto> journalpostDtoList = new ArrayList<>();

		journalpostDtoList.add(createJournalpostDtoJournalpost(FS, TEMA_KTR, PEN, IDENT, AKTOER_ID, BID, I));
		journalpostDtoList.add(createJournalpostDtoJournalpost(MO, TEMA_PEN, PEN, IDENT, AKTOER_ID, KTR, I));

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(journalpostDtoList, brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostIsNotatAndNotForvaltningsnotat() {
		JournalpostDto journalpostDto = createJournalpostDtoJournalpost(M, TEMA_PEN, PEN, IDENT, AKTOER_ID, BID, N);

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}

	@Test
	void shouldNotReturnJournalpostWhenJournalpostIsOrganinternt() {
		JournalpostDto journalpostDto = createJournalpostDtoBase().dokumenter(List.of(DokumentInfoDto.builder().organInternt(true).build())).build();

		List<JournalpostDto> reducedJournalpostDtoList = utledTilgangDokumentoversiktService.utledTilgangJournalposter(List.of(journalpostDto), brukerIdenter);

		assertEquals(0, reducedJournalpostDtoList.size());
	}*/
}

