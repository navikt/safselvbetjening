package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.I;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.N;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.NAV_NO;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_NETS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.POL;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KASSERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.IDENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.brukerIdenter;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createDokumentinfoDtoDokument;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.createJournalpostDtoDokument;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.mockBrukerIdenter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtledTilgangDokumentServiceTest {

	private final UtledTilgangDokumentService utledTilgangDokumentService = new UtledTilgangDokumentService();

	@BeforeAll
	static void setup() {
		mockBrukerIdenter();
	}

	@Test
	void utledTilgangDokumentHappyPath() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(I, IDENT, NAV_NO);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, false, false, null);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);

		assertTrue(feilmeldinger.isEmpty());
	}

	@Test
	void utledTilgangDokumentWithAnnenPartAndNotat() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(N, ANNEN_PART, NAV_NO);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, false, false, null);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);

		assertTrue(feilmeldinger.isEmpty());
	}

	@Test
	void shouldReturnFeilmeldingPartsinnsyn() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(I, ANNEN_PART, NAV_NO);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, false, false, null);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);

		assertEquals(1, feilmeldinger.size());
		assertEquals(PARTSINNSYN, feilmeldinger.get(0));
	}

	@Test
	void shouldReturnFeilmeldingSkannetDokument() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(N, IDENT, SKAN_NETS);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, false, false, null);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);

		assertEquals(1, feilmeldinger.size());
		assertEquals(SKANNET_DOKUMENT, feilmeldinger.get(0));
	}

	@Test
	void shouldReturnFeilmeldingInnskrenketPartsinnsyn() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(N, IDENT, NAV_NO);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, true, false, null);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);

		assertEquals(1, feilmeldinger.size());
		assertEquals(INNSKRENKET_PARTSINNSYN, feilmeldinger.get(0));
	}

	@Test
	void shouldReturnFeilmeldingGDPR() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(N, IDENT, NAV_NO);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, false, false, POL);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);
		assertEquals(1, feilmeldinger.size());
		assertEquals(GDPR, feilmeldinger.get(0));
	}

	@Test
	void shouldReturnFeilmeldingKassert() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(N, IDENT, NAV_NO);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(false, false, true, null);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);
		assertEquals(1, feilmeldinger.size());
		assertEquals(KASSERT, feilmeldinger.get(0));
	}

	@Test
	void shouldReturnAllFeilmeldinger() {
		JournalpostDto journalpostDto = createJournalpostDtoDokument(I, ANNEN_PART, SKAN_NETS);
		DokumentInfoDto dokumentInfoDto = createDokumentinfoDtoDokument(true, true, true, POL);

		List<String> feilmeldinger = utledTilgangDokumentService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter);
		assertEquals(5, feilmeldinger.size());
		assertTrue(feilmeldinger.contains(PARTSINNSYN));
		assertTrue(feilmeldinger.contains(SKANNET_DOKUMENT));
		assertTrue(feilmeldinger.contains(INNSKRENKET_PARTSINNSYN));
		assertTrue(feilmeldinger.contains(GDPR));
		assertTrue(feilmeldinger.contains(KASSERT));
	}
}