package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES_BRUKERS_ONSKE;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES_FEILSENDT;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES_INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES_ORGAN_INTERNT;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.VISES_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.VISES_MANUELT_GODKJENT;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.VISES_MASKINELT_GODKJENT;
import static no.nav.safselvbetjening.tilgang.TilgangJournalposttype.ANNEN;
import static no.nav.safselvbetjening.tilgang.TilgangJournalposttype.NOTAT;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.FERDIGSTILT;
import static no.nav.safselvbetjening.tilgang.TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK;
import static no.nav.safselvbetjening.tilgang.TilgangMottakskanal.SKANNING;
import static no.nav.safselvbetjening.tilgang.TilgangMottakskanal.TEKNISK;
import static no.nav.safselvbetjening.tilgang.TilgangSkjermingType.INGEN_SKJERMING;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.IDENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_ARBEIDSRAADGIVNING_SKJERMET;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_DAGPENGER;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_FARSKAP;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KONTROLL;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KONTROLL_ANMELDELSE;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_PENSJON;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_UFOR;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseJournalfoertJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseMottattJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseTilgangDokument;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseTilgangJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseTilgangVariant;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.defaultBrukerIdenter;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.tilgangDokument;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.tilgangVariant;
import static org.assertj.core.api.Assertions.assertThat;

class UtledTilgangServiceTest {

	private static final LocalDateTime FOER_TIDLIGSTE_INNSYNSDATO = UtledTilgangService.TIDLIGST_INNSYN_DATO.minusMinutes(1);
	private static final String FORVALTNINGSNOTAT = "FORVALTNINGSNOTAT";
	private static final String SKAN_IM = "SKAN_IM";
	private static final String SKAN_NETS = "SKAN_NETS";
	private static final String SKAN_PEN = "SKAN_PEN";
	private final UtledTilgangService utledTilgangService;

	public UtledTilgangServiceTest() {
		utledTilgangService = new UtledTilgangService();
	}

	@Test
	void shouldReturnEmptyListWhenTilgangTilJournalpost() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenTilgangTilJournalpostWithInnsynBrukerStandardRegler() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenTilgangTilJournalpostWithInnsynIsVises() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_FARSKAP, VISES_MANUELT_GODKJENT).build(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnSkjultInnsynReasonWhenTilgangTilJournalpostWithInnsynIsSkjult() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, SKJULES_ORGAN_INTERNT).build(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_SKJULT_INNSYN);
	}

	@Test
	void shouldReturnInnsynsdatoReasonWhenJournalpostJournaldatoFoerOpprettetDato() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.journalfoertDato(FOER_TIDLIGSTE_INNSYNSDATO)
				.build(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_FOER_INNSYNSDATO);
	}

	@Test
	void shouldReturnEmptyListWhenTilgangTilDokument() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(), tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenTilgangTilDokumentWithInnsynBrukerStandardRegler() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(), tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnEmptyListWhenTilgangTilDokumentWithInnsynIsVises() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_FARSKAP, VISES_MANUELT_GODKJENT).build(), tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnInnsynsdatoWhenJournalpostDokumentFoerOpprettetDato() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).journalfoertDato(FOER_TIDLIGSTE_INNSYNSDATO).build(),
				tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_FOER_INNSYNSDATO);
	}

	@Test
	void shouldReturnGDPRWhenTilgangTilDokumentWithSkjermingPOL() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(),
				baseTilgangDokument().skjerming(TilgangSkjermingType.POL).build(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_POL_GDPR);
	}

	@Test
	void shouldReturnGDPRWhenTilgangVariantWithSkjermingPOL() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(),
				tilgangDokument(), baseTilgangVariant().skjerming(TilgangSkjermingType.POL).build(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_POL_GDPR);
	}

	@Test
	void shouldReturnDenyKassertWhenTilgangDokumentKassert() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(),
				baseTilgangDokument().kassert(true).build(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_KASSERT);
	}

	@Test
	void shouldReturnDenySkannetWhenDokumentSkannet() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).mottakskanal(SKANNING).build(),
				tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_SKANNET_DOKUMENT);
	}

	@Test
	void shouldReturnDenyTekniskWhenMottakskanalDokumentTeknisk() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).mottakskanal(TEKNISK).build(),
				tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_TEKNISK_DOKUMENT);
	}

	@Test
	void shouldReturnDenyTekniskWhenUtsendingskanalDokumentTeknisk() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).utsendingskanal(TilgangUtsendingskanal.TEKNISK).build(),
				tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_TEKNISK_DOKUMENT);
	}

	@Test
	void shouldReturnEmptyListWhenDokumentSkannetAndInnsynVises() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).mottakskanal(SKANNING).innsyn(VISES_MANUELT_GODKJENT).build(),
				tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnUgyldigVariantWhenTilgangVariantNotArkivOrSladdet() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build(),
				tilgangDokument(), baseTilgangVariant().variantformat(TilgangVariantFormat.from("ORIGINAL")).build(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_UGYLDIG_VARIANTFORMAT);
	}

	@Test
	void shouldReturnAnnenPartWhenDokumentBelongsToAnnenPart() {
		var tilgang = utledTilgangService.utledTilgangDokument(baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
						.avsenderMottakerId(ANNEN_PART)
						.build(),
				tilgangDokument(), tilgangVariant(), defaultBrukerIdenter());
		assertThat(tilgang).containsExactly(TilgangDenyReason.DENY_REASON_IKKE_AVSENDER_MOTTAKER);
	}


	//	1a - Bruker må være part for å se journalposter
	// 	Mottatt - ingen sakstilknytning eller bruker
	@Test
	void shouldReturnFalseWhenMottattAndBrukerPartIsNull() {
		boolean brukerPart = utledTilgangService.isBrukerPart(baseMottattJournalpost().build(), defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Mottatt - ingen sakstilknytning
	@Test
	void shouldReturnTrueWhenMottattAndBrukerPartIsSet() {
		TilgangJournalpost journalpost = baseMottattJournalpost()
				.tilgangBruker(new TilgangBruker(IDENT))
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_DAGPENGER)
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Mottatt - annen bruker
	@Test
	void shouldReturnFalseWhenMottattAndBrukerPartIsSet() {
		TilgangJournalpost journalpost = baseMottattJournalpost()
				.tilgangBruker(new TilgangBruker(ANNEN_PART))
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_DAGPENGER)
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i gsak (FS22)
	@Test
	void shouldReturnTrueWhenJournalfoertInGsakAndBrukerPart() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i pensjon (PEN)
	@Test
	void shouldReturnTrueWhenJournalfoertInPensjonAndBrukerPart() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.tilgangBruker(null)
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_PENSJON)
				.journalfoertDato(LocalDateTime.now())
				.tilgangSak(TilgangSak.builder()
						.ident(IDENT)
						.feilregistrert(false)
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i gsak (FS22)
	@Test
	void shouldReturnFalseWhenJournalfoertInGsakAndAnnenPart() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.tilgangBruker(new TilgangBruker(ANNEN_PART))
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_DAGPENGER)
				.journalfoertDato(LocalDateTime.now())
				.tilgangSak(TilgangSak.builder()
						.ident(Ident.of(ANNEN_AKTOER_ID))
						.feilregistrert(false)
						.tema(TEMA_DAGPENGER)
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i pensjon (PEN)
	@Test
	void shouldReturnFalseWhenJournalfoertInPensjonAndAnnenPart() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.tilgangBruker(new TilgangBruker(ANNEN_PART))
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_PENSJON)
				.journalfoertDato(LocalDateTime.now())
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.tilgangSak(TilgangSak.builder()
						.feilregistrert(false)
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato
	@Test
	void shouldReturnFalseWhenJournalfoertBeforeInnsynsdatoAndStartedWithVises() {
		final LocalDateTime journalfoertDato = LocalDateTime.of(2016, 5, 6, 0, 0);
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.datoOpprettet(journalfoertDato)
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.journalfoertDato(journalfoertDato)
				.innsyn(VISES_MANUELT_GODKJENT)
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVisesOrTemaPensjonUforetrygd(journalpost);
		assertThat(actual).isFalse();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato og hvis innsyn er BRUK_STANDARDREGLER.
	@Test
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdatoAndInnsynWithBrukStandardRegler() {
		final LocalDateTime journalfoertDato = LocalDateTime.of(2016, 5, 6, 0, 0);
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.datoOpprettet(journalfoertDato)
				.journalfoertDato(journalfoertDato)
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(BRUK_STANDARDREGLER)
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVisesOrTemaPensjonUforetrygd(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato og hvis innsyn er null.
	@Test
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdatoAndInnsynIsNull() {
		final LocalDateTime journalfoertDato = LocalDateTime.of(2016, 5, 6, 0, 0);
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(journalfoertDato)
				.journalfoertDato(journalfoertDato)
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVisesOrTemaPensjonUforetrygd(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Opprettet før innsynsdato
	@Test
	void shouldReturnTrueWhenOpprettetBeforeInnsynsdatoAndNotStartedWithVISES() {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.journalfoertDato(LocalDateTime.of(2017, 5, 6, 0, 0))
				.datoOpprettet(LocalDateTime.of(2015, 5, 6, 0, 0))
				.innsyn(SKJULES_BRUKERS_ONSKE)
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVisesOrTemaPensjonUforetrygd(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// 	Unntak når tema er PEN eller UFO
	@ParameterizedTest
	@ValueSource(strings = {TEMA_PENSJON, TEMA_UFOR})
	void shouldReturnFalseWhenOpprettetBeforeInnsynsdatoAndTemaIsExceptFromDateRule(String tema) {
		final LocalDateTime journalfoertDato = LocalDateTime.of(2016, 5, 6, 0, 0);
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(journalfoertDato)
				.journalfoertDato(journalfoertDato)
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.tema(tema)
				.build();
		boolean actual = new UtledTilgangService(true).isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVisesOrTemaPensjonUforetrygd(journalpost);
		assertThat(actual).isFalse();
	}


	//	1c - Bruker får kun se midlertidige og ferdigstilte journalposter
	@Test
	void shouldReturnFalseWhenNotJournalfoertOrMottatt() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER).journalstatus(TilgangJournalstatus.ANNEN).build();
		boolean actual = utledTilgangService.isJournalpostFerdigstiltOrMidlertidig(journalpost);
		assertThat(actual).isFalse();
	}

	//	1d - Bruker får ikke se feilregistrerte journalposter
	@Test
	void shouldReturnTrueWhenFeilregistrert() {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.tilgangSak(TilgangSak.builder()
						.feilregistrert(true)
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalpostFeilregistrert(journalpost);
		assertThat(actual).isTrue();
	}

	// 1e) Bruker får ikke se journalposter på følgende tema:
	// * KTR (Kontroll)
	// * FAR (Farskap)
	// * KTA (Kontroll anmeldelse)
	// * ARS (Arbeidsrådgivning skjermet)
	// * ARP (Arbeidsrådgivning psykologstester)
	// * med mindre k_innsyn = VISES_*
	// Mottatt - ingen sakstilknytning
	@ParameterizedTest
	@ValueSource(strings = {TEMA_FARSKAP, TEMA_KONTROLL, TEMA_KONTROLL_ANMELDELSE, TEMA_ARBEIDSRAADGIVNING_SKJERMET, TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER})
	void shouldReturnFalseWhenMottattAndKontrollsakOrFarskapssak(String tema) {
		TilgangJournalpost journalpost = baseMottattJournalpost()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.tema(tema)
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)).isFalse();
	}

	// 1e) Bruker får ikke se journalposter på følgende tema:
	// * KTR (Kontroll)
	// * FAR (Farskap)
	// * KTA (Kontroll anmeldelse)
	// * ARS (Arbeidsrådgivning skjermet)
	// * ARP (Arbeidsrådgivning psykologstester)
	// * med mindre k_innsyn = VISES_*
	// Journalført - med sakstilknytning
	@ParameterizedTest
	@ValueSource(strings = {TEMA_FARSKAP, TEMA_KONTROLL, TEMA_KONTROLL_ANMELDELSE, TEMA_ARBEIDSRAADGIVNING_SKJERMET, TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER})
	void shouldReturnFalseWhenJournalfoertAndSakTemaIkkeInnsynForBruker(String tema) {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(tema, SKJULES_BRUKERS_ONSKE)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.tema(tema)
				.tilgangSak(TilgangSak.builder()
						.ident(Ident.of(AKTOER_ID))
						.feilregistrert(false)
						.tema(tema)
						.build())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)).isFalse();
	}

	// 1e) Bruker får ikke se journalposter på følgende tema:
	// * KTR (Kontroll)
	// * FAR (Farskap)
	// * KTA (Kontroll anmeldelse)
	// * ARS (Arbeidsrådgivning skjermet)
	// * ARP (Arbeidsrådgivning psykologstester)
	// * med mindre k_innsyn = VISES_*
	// Journalført - uten sakstilknytning
	@ParameterizedTest
	@ValueSource(strings = {TEMA_FARSKAP, TEMA_KONTROLL, TEMA_KONTROLL_ANMELDELSE, TEMA_ARBEIDSRAADGIVNING_SKJERMET, TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER})
	void shouldReturnFalseWhenJournalfoertAndKontrollOrFarskapssakWithoutSak(String tema) {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(tema, BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)).isFalse();
	}


	//	1f - Bruker får ikke innsyn i journalposter som er begrenset ihht. GDPR
	@Test
	void shouldReturnFalseWhenBegrensetWithGdpr() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.skjerming(TilgangSkjermingType.POL)
				.build();
		boolean actual = utledTilgangService.isJournalpostGDPRRestricted(journalpost);
		assertThat(actual).isTrue();
	}

	//	1g - Bruker får ikke innsyn i notater (jp.type = N) med mindre det er et forvaltningsnotat
	@Test
	void shouldReturnTrueWhenForvaltningsnotat() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.journalposttype(NOTAT)
				.dokumenter(List.of(
						TilgangDokument.builder()
								.skjerming(INGEN_SKJERMING)
								.kategori(FORVALTNINGSNOTAT)
								.build()))
				.build();
		boolean actual = utledTilgangService.isJournalpostNotatXNORForvaltningsnotat(journalpost);
		assertThat(actual).isTrue();
	}

	//	1i) Bruker kan ikke få se journalposter som innsyn begynner med SKJULES_*
	@ParameterizedTest
	@EnumSource(value = TilgangInnsyn.class, names = {
			"SKJULES_INNSKRENKET_PARTSINNSYN", "SKJULES_ORGAN_INTERNT", "SKJULES_FEILSENDT", "SKJULES_BRUKERS_ONSKE", "SKJULES_BRUKERS_SIKKERHET"
	})
	void shouldReturnTrueWhenJournalpostInnsynSkjult(TilgangInnsyn innsyn) {
		TilgangJournalpost journalpost = getBaseJournalfoertJournalpostWithInnsyn(innsyn);
		boolean actual = utledTilgangService.isJournalpostInnsynSkjules(journalpost);
		assertThat(actual).isTrue();
	}

	//	1i)  Bruker kan få se journalposter Hvis innsyn begynner ikke med SKJULES_*
	@ParameterizedTest
	@EnumSource(value = TilgangInnsyn.class, names = {"VISES_FORVALTNINGSNOTAT", "VISES_MANUELT_GODKJENT", "VISES_MASKINELT_GODKJENT", "BRUK_STANDARDREGLER"})
	void shouldReturnFalseWhenJournalpostInnsynErIKkeSkjult(TilgangInnsyn innsyn) {
		TilgangJournalpost journalpost = getBaseJournalfoertJournalpostWithInnsyn(innsyn);
		boolean actual = utledTilgangService.isJournalpostInnsynSkjules(journalpost);
		assertThat(actual).isFalse();
	}

	//	2a - Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	@Test
	void shouldReturnFalseWhenAvsenderMottakerIdIsNull() {
		TilgangJournalpost journalpost = baseTilgangJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.avsenderMottakerId(null).build();
		boolean actual = utledTilgangService.isAvsenderMottakerPart(journalpost, defaultBrukerIdenter());
		assertThat(actual).isFalse();
	}

	//	2a - Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	@Test
	void shouldReturnFalseWhenAvsenderMottakerIdIsAnnenPart() {
		TilgangJournalpost journalpost = baseTilgangJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.avsenderMottakerId(ANNEN_PART).build();
		boolean actual = utledTilgangService.isAvsenderMottakerPart(journalpost, defaultBrukerIdenter());
		assertThat(actual).isFalse();
	}

	//	2b - Bruker får ikke se skannede dokumenter, med mindre K_INNSYN = VISES_*
	@Test
	void shouldReturnTrueWhenSkannetDokument() {
		boolean imSkannetDokument = isSkannetDokument(SKAN_IM, SKJULES_BRUKERS_ONSKE);
		boolean imSkannetDokumentInnsynNull = isSkannetDokument(SKAN_IM, BRUK_STANDARDREGLER);
		boolean netsSkannetDokument = isSkannetDokument(SKAN_NETS, SKJULES_INNSKRENKET_PARTSINNSYN);
		boolean penSkannetDokument = isSkannetDokument(SKAN_PEN, SKJULES_ORGAN_INTERNT);

		assertThat(imSkannetDokument).isTrue();
		assertThat(imSkannetDokumentInnsynNull).isTrue();
		assertThat(netsSkannetDokument).isTrue();
		assertThat(penSkannetDokument).isTrue();
	}

	//	2b - Bruker får ikke se skannede dokumenter - lokal utskrift - journalposttype U - mottakskanal satt
	@Test
	void shouldReturnTrueWhenSkannetDokumentAndLokalUtskrift() {
		assertSkannetDokumentLokalUtskrift(SKAN_IM, BRUK_STANDARDREGLER);
		assertSkannetDokumentLokalUtskrift(SKAN_NETS, SKJULES_FEILSENDT);
		assertSkannetDokumentLokalUtskrift(SKAN_PEN, SKJULES_ORGAN_INTERNT);
	}

	//	2b - Bruker får se skannede dokumenter og innsyn starter med VISES_*
	@Test
	void shouldReturnFalseWhenSkannetDokumentAndStartsWithVises() {
		boolean imSkannetDokument = isSkannetDokument(SKAN_IM, VISES_MANUELT_GODKJENT);
		boolean netsSkannetDokument = isSkannetDokument(SKAN_NETS, VISES_FORVALTNINGSNOTAT);
		boolean penSkannetDokument = isSkannetDokument(SKAN_PEN, VISES_MASKINELT_GODKJENT);

		assertThat(imSkannetDokument).isFalse();
		assertThat(netsSkannetDokument).isFalse();
		assertThat(penSkannetDokument).isFalse();
	}

	//	2c - Bruker får ikke se dokumenter som er mottatt i tekniske kanaler.
	@ParameterizedTest
	@ValueSource(strings = {"ALTINN", "ALTINN_INNBOKS", "EESSI", "EIA", "EKST_OPPS", "HELSENETTET"})
	void shouldReturnTrueWhenTekniskMottakskanal(String mottakskanal) {
		assertThat(isTekniskMottakskanal(mottakskanal)).isTrue();
	}

	//	2c - Bruker får ikke se dokumenter som er sendt fra tekniske kanaler.
	@ParameterizedTest
	@ValueSource(strings = {"ALTINN", "EESSI", "EIA", "HELSENETTET", "TRYGDERETTEN"})
	void shouldReturnTrueWhenTekniskUtsendingskanal(String utsendingskanal) {
		assertThat(isTekniskUtsendingskanal(utsendingskanal)).isTrue();
	}

	//	2e - Dokumenter som er begrenset ihht. gdpr
	@Test
	void shouldReturnTrueWhenDokumentvariantIsPolSkjermet() {
		TilgangDokument dokumentInfo = TilgangDokument.builder()
				.skjerming(TilgangSkjermingType.POL)
				.build();
		boolean actual = utledTilgangService.isDokumentGDPRRestricted(dokumentInfo);
		assertThat(actual).isTrue();
	}

	//	2e - Dokumentervariant som er begrenset ihht. gdpr
	@Test
	void shouldReturnTrueWhenDokumentVariantIsPolSkjermet() {
		TilgangDokument dokumentInfo = TilgangDokument.builder()
				.skjerming(TilgangSkjermingType.POL)
				.dokumentvarianter(List.of(TilgangVariant.builder()
						.skjerming(TilgangSkjermingType.POL)
						.variantformat(TilgangVariantFormat.ARKIV)
						.build()))
				.build();
		boolean actual = utledTilgangService.isDokumentvariantGDPRRestricted(dokumentInfo.dokumentvarianter().getFirst());
		assertThat(actual).isTrue();
	}

	//	2f - Kasserte dokumenter skal ikke vises
	@Test
	void shouldReturnWhenDokumentIsKassert() {
		boolean actual = utledTilgangService.isDokumentKassert(
				TilgangDokument.builder()
						.skjerming(INGEN_SKJERMING)
						.kassert(true)
						.build());
		assertThat(actual).isTrue();
	}


	@Test
	void shouldReturnFalseWhenGjeldendeTemaIsUnntattInnsyn() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.tema("KTA")
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.journalstatus(FERDIGSTILT)
				.tilgangSak(TilgangSak.builder()
						.tema("KTA")
						.build())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(tilgangJournalpost)).isFalse();
	}

	@Test
	void shouldReturnTrueWhenGjeldendeTemaIsNotUnntattInnsyn() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.tema("DAG")
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.journalstatus(FERDIGSTILT)
				.tilgangSak(TilgangSak.builder()
						.tema("DAG")
						.build())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(tilgangJournalpost)).isTrue();
	}

	private boolean isSkannetDokument(String mottakskanal, TilgangInnsyn innsyn) {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.mottakskanal(TilgangMottakskanal.from(mottakskanal))
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.datoOpprettet(LocalDateTime.now())
				.innsyn(innsyn)
				.build();
		return utledTilgangService.isSkannetDokumentAndInnsynIsNotVises(journalpost);
	}

	private boolean isTekniskMottakskanal(String mottakskanal) {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.mottakskanal(TilgangMottakskanal.from(mottakskanal))
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.datoOpprettet(LocalDateTime.now())
				.build();
		return utledTilgangService.isTekniskDokumentKanal(journalpost);
	}

	private boolean isTekniskUtsendingskanal(String utsendingskanal) {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.mottakskanal(IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.from(utsendingskanal))
				.datoOpprettet(LocalDateTime.now())
				.build();
		return utledTilgangService.isTekniskDokumentKanal(journalpost);
	}

	private void assertSkannetDokumentLokalUtskrift(String kanal, TilgangInnsyn innsyn) {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.journalposttype(ANNEN)
				.mottakskanal(TilgangMottakskanal.from(kanal))
				.innsyn(innsyn)
				.build();
		boolean ironMountainActual = utledTilgangService.isSkannetDokumentAndInnsynIsNotVises(journalpost);
		assertThat(ironMountainActual).isTrue();
	}

	private TilgangJournalpost getBaseJournalfoertJournalpostWithInnsyn(TilgangInnsyn innsyn) {
		return baseJournalfoertJournalpost(TEMA_DAGPENGER, innsyn)
				.dokumenter(List.of(
						TilgangDokument.builder().skjerming(INGEN_SKJERMING).build(),
						TilgangDokument.builder().skjerming(INGEN_SKJERMING).build()
				))
				.build();
	}

}