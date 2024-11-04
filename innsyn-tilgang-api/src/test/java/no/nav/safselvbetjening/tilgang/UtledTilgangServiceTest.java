package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
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
import static no.nav.safselvbetjening.tilgang.TilgangJournalposttype.N;
import static no.nav.safselvbetjening.tilgang.TilgangJournalposttype.U;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.FERDIGSTILT;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.UNDER_ARBEID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ARKIVSAKSYSTEM_GOSYS;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ARKIVSAKSYSTEM_PENSJON;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.FOER_INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.IDENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_ARBEIDSRAADGIVNING_SKJERMET;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_DAGPENGER;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_FARSKAP;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KONTROLL;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KONTROLL_ANMELDELSE;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_PENSJON;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseJournalfoertJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseMottattJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseTilgangJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.defaultBrukerIdenter;
import static org.assertj.core.api.Assertions.assertThat;

class UtledTilgangServiceTest {

	private static final String FORVALTNINGSNOTAT = "FORVALTNINGSNOTAT";
	private static final String SKAN_IM = "SKAN_IM";
	private static final String SKAN_NETS = "SKAN_NETS";
	private static final String SKAN_PEN = "SKAN_PEN";
	private final UtledTilgangService utledTilgangService;

	public UtledTilgangServiceTest() {
		utledTilgangService = new UtledTilgangService(LocalDate.of(2016, 6, 4));
	}

	@Test
	void shouldReturnTrueWhenTilgangTilJournalpost() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, null).build(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnTrueWhenTilgangTilJournalpostWithInnsynBrukerStandardRegler() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, null).build(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnTrueWhenTilgangTilJournalpostWithInnsynIsVises() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_FARSKAP, VISES_MANUELT_GODKJENT).build(), defaultBrukerIdenter());
		assertThat(tilgang).isEmpty();
	}

	@Test
	void shouldReturnFalseeWhenTilgangTilJournalpostWithInnsynIsSjult() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, SKJULES_ORGAN_INTERNT).build(), defaultBrukerIdenter());
		assertThat(tilgang).isNotEmpty();
	}

	@Test
	void shouldReturnFalseWhenJournalpostJournaldatoFoerOpprettetDato() {
		var tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.journalfoertDato(FOER_INNSYNSDATO)
				.build(), defaultBrukerIdenter());
		assertThat(tilgang).isNotEmpty();
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
				.tilgangBruker(new TilgangBruker(Foedselsnummer.of(IDENT)))
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
				.tilgangBruker(new TilgangBruker(Foedselsnummer.of(ANNEN_PART)))
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
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null).build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i pensjon (PEN)
	@Test
	void shouldReturnTrueWhenJournalfoertInPensjonAndBrukerPart() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgangBruker(null)
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_PENSJON)
				.journalfoertDato(LocalDateTime.now())
				.tilgangSak(TilgangSak.builder()
						.foedselsnummer(Foedselsnummer.of(IDENT))
						.fagsystem(ARKIVSAKSYSTEM_PENSJON)
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
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgangBruker(new TilgangBruker(Foedselsnummer.of(ANNEN_PART)))
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_DAGPENGER)
				.journalfoertDato(LocalDateTime.now())
				.tilgangSak(TilgangSak.builder()
						.aktoerId(AktoerId.of(ANNEN_AKTOER_ID))
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
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
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgangBruker(new TilgangBruker(Foedselsnummer.of(ANNEN_PART)))
				.datoOpprettet(LocalDateTime.now())
				.tema(TEMA_PENSJON)
				.journalfoertDato(LocalDateTime.now())
				.tilgangSak(TilgangSak.builder()
						.fagsystem(ARKIVSAKSYSTEM_PENSJON)
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
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
				.innsyn(VISES_MANUELT_GODKJENT)
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isFalse();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato og hvis innsyn er BRUK_STANDARDREGLER.
	@Test
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdatoAndInnsynWithBrukStandardRegler() {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
				.innsyn(BRUK_STANDARDREGLER)
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato og hvis innsyn er null.
	@Test
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdatoAndInnsynIsNull() {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
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
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isTrue();
	}

	//	1c - Bruker får kun se midlertidige og ferdigstilte journalposter
	@Test
	void shouldReturnFalseWhenNotJournalfoertOrMottatt() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null).journalstatus(UNDER_ARBEID).build();
		boolean actual = utledTilgangService.isJournalpostFerdigstiltOrMidlertidig(journalpost);
		assertThat(actual).isFalse();
	}

	//	1d - Bruker får ikke se feilregistrerte journalposter
	@Test
	void shouldReturnTrueWhenFeilregistrert() {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
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
				.tema(tema)
				.tilgangSak(TilgangSak.builder()
						.aktoerId(AktoerId.of(AKTOER_ID))
						.fagsystem(ARKIVSAKSYSTEM_GOSYS)
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
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(tema, null)
				.datoOpprettet(LocalDateTime.now())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)).isFalse();
	}


	//	1f - Bruker får ikke innsyn i journalposter som er begrenset ihht. GDPR
	@Test
	void shouldReturnFalseWhenBegrensetWithGdpr() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.skjerming(TilgangSkjermingType.POL)
				.build();
		boolean actual = utledTilgangService.isJournalpostGDPRRestricted(journalpost);
		assertThat(actual).isTrue();
	}

	//	1g - Bruker får ikke innsyn i notater (jp.type = N) med mindre det er et forvaltningsnotat
	@Test
	void shouldReturnTrueWhenForvaltningsnotat() {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.journalposttype(N)
				.dokumenter(List.of(
						TilgangDokument.builder()
								.kategori(FORVALTNINGSNOTAT)
								.build()))
				.build();
		boolean actual = utledTilgangService.isJournalpostForvaltningsnotat(journalpost);
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
		TilgangJournalpost journalpost = baseTilgangJournalpost(TEMA_DAGPENGER, null)
				.avsenderMottakerId(null).build();
		boolean actual = utledTilgangService.isAvsenderMottakerPart(journalpost, defaultBrukerIdenter());
		assertThat(actual).isFalse();
	}

	//	2a - Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	@Test
	void shouldReturnFalseWhenAvsenderMottakerIdIsAnnenPart() {
		TilgangJournalpost journalpost = baseTilgangJournalpost(TEMA_DAGPENGER, null)
				.avsenderMottakerId(ANNEN_PART).build();
		boolean actual = utledTilgangService.isAvsenderMottakerPart(journalpost, defaultBrukerIdenter());
		assertThat(actual).isFalse();
	}

	//	2b - Bruker får ikke se skannede dokumenter, med mindre K_INNSYN = VISES_*
	@Test
	void shouldReturnTrueWhenSkannetDokument() {
		boolean imSkannetDokument = isSkannetDokument(SKAN_IM, SKJULES_BRUKERS_ONSKE);
		boolean imSkannetDokumentInnsynNull = isSkannetDokument(SKAN_IM, null);
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
						.kassert(true)
						.build());
		assertThat(actual).isTrue();
	}


	@Test
	void shouldReturnFalseWhenGjeldendeTemaIsUnntattInnsyn() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.tema("KTA")
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
				.journalstatus(FERDIGSTILT)
				.tilgangSak(TilgangSak.builder()
						.tema("DAG")
						.build())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(tilgangJournalpost)).isTrue();
	}

	private boolean isSkannetDokument(String kanal, TilgangInnsyn innsyn) {
		TilgangJournalpost journalpost = TilgangJournalpost.builder()
				.mottakskanal(kanal)
				.innsyn(innsyn)
				.build();
		return utledTilgangService.isSkannetDokumentAndInnsynIsNotVises(journalpost);
	}

	private void assertSkannetDokumentLokalUtskrift(String kanal, TilgangInnsyn innsyn) {
		TilgangJournalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.journalposttype(U)
				.mottakskanal(kanal)
				.innsyn(innsyn)
				.build();
		boolean ironMountainActual = utledTilgangService.isSkannetDokumentAndInnsynIsNotVises(journalpost);
		assertThat(ironMountainActual).isTrue();
	}

	private TilgangJournalpost getBaseJournalfoertJournalpostWithInnsyn(TilgangInnsyn innsyn) {
		return baseJournalfoertJournalpost(TEMA_DAGPENGER, innsyn)
				.dokumenter(List.of(
						TilgangDokument.builder().build(),
						TilgangDokument.builder()
								.build()
				))
				.build();
	}

}