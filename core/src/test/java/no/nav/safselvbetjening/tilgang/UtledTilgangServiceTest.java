package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Innsyn;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.domain.Innsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_BRUKERS_ONSKE;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_FEILSENDT;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_ORGAN_INTERNT;
import static no.nav.safselvbetjening.domain.Innsyn.VISES_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.domain.Innsyn.VISES_MANUELT_GODKJENT;
import static no.nav.safselvbetjening.domain.Innsyn.VISES_MASKINELT_GODKJENT;
import static no.nav.safselvbetjening.domain.Innsyn.valueOf;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_IM;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_NETS;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_PEN;
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

	private final UtledTilgangService utledTilgangService;

	public UtledTilgangServiceTest() {
		SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();
		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));
		utledTilgangService = new UtledTilgangService(safSelvbetjeningProperties);
	}

	@Test
	void shouldReturnTrueWhenTilgangTilJournalpost() {
		boolean tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, null).build(), defaultBrukerIdenter());
		assertThat(tilgang).isTrue();
	}

	@Test
	void shouldReturnTrueWhenTilgangTilJournalpostWithInnsynBrukerStandardRegler() {
		boolean tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, null).build(), defaultBrukerIdenter());
		assertThat(tilgang).isTrue();
	}

	@Test
	void shouldReturnTrueWhenTilgangTilJournalpostWithInnsynIsVises() {
		boolean tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_FARSKAP, VISES_MANUELT_GODKJENT).build(), defaultBrukerIdenter());
		assertThat(tilgang).isTrue();
	}

	@Test
	void shouldReturnFalseeWhenTilgangTilJournalpostWithInnsynIsSjult() {
		boolean tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, SKJULES_ORGAN_INTERNT).build(), defaultBrukerIdenter());
		assertThat(tilgang).isFalse();
	}

	@Test
	void shouldReturnFalseWhenJournalpostJournaldatoFoerOpprettetDato() {
		boolean tilgang = utledTilgangService.utledTilgangJournalpost(baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(baseTilgangJournalpost(TEMA_DAGPENGER, null)
						.journalfoertDato(FOER_INNSYNSDATO)
						.build())
				.build(), defaultBrukerIdenter());
		assertThat(tilgang).isFalse();
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
		Journalpost journalpost = baseMottattJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangBruker(Journalpost.TilgangBruker.builder()
								.brukerId(IDENT)
								.build())
						.datoOpprettet(LocalDateTime.now())
						.tema(TEMA_DAGPENGER)
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Mottatt - annen bruker
	@Test
	void shouldReturnFalseWhenMottattAndBrukerPartIsSet() {
		Journalpost journalpost = baseMottattJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangBruker(Journalpost.TilgangBruker.builder()
								.brukerId(ANNEN_PART)
								.build())
						.datoOpprettet(LocalDateTime.now())
						.tema(TEMA_DAGPENGER)
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i gsak (FS22)
	@Test
	void shouldReturnTrueWhenJournalfoertInGsakAndBrukerPart() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null).build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i pensjon (PEN)
	@Test
	void shouldReturnTrueWhenJournalfoertInPensjonAndBrukerPart() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangBruker(null)
						.datoOpprettet(LocalDateTime.now())
						.tema(TEMA_PENSJON)
						.journalfoertDato(LocalDateTime.now())
						.tilgangSak(Journalpost.TilgangSak.builder()
								.foedselsnummer(IDENT)
								.fagsystem(ARKIVSAKSYSTEM_PENSJON)
								.feilregistrert(false)
								.build())
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i gsak (FS22)
	@Test
	void shouldReturnFalseWhenJournalfoertInGsakAndAnnenPart() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangBruker(Journalpost.TilgangBruker.builder()
								.brukerId(ANNEN_PART)
								.build())
						.datoOpprettet(LocalDateTime.now())
						.tema(TEMA_DAGPENGER)
						.journalfoertDato(LocalDateTime.now())
						.tilgangSak(Journalpost.TilgangSak.builder()
								.aktoerId(ANNEN_AKTOER_ID)
								.fagsystem(ARKIVSAKSYSTEM_GOSYS)
								.feilregistrert(false)
								.tema(TEMA_DAGPENGER)
								.build())
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i pensjon (PEN)
	@Test
	void shouldReturnFalseWhenJournalfoertInPensjonAndAnnenPart() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangBruker(Journalpost.TilgangBruker.builder()
								.brukerId(ANNEN_PART)
								.build())
						.datoOpprettet(LocalDateTime.now())
						.tema(TEMA_PENSJON)
						.journalfoertDato(LocalDateTime.now())
						.tilgangSak(Journalpost.TilgangSak.builder()
								.fagsystem(ARKIVSAKSYSTEM_PENSJON)
								.feilregistrert(false)
								.build())
						.build())
				.build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isFalse();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato
	@Test
	void shouldReturnFalseWhenJournalfoertBeforeInnsynsdatoAndStartedWithVises() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, VISES_MANUELT_GODKJENT)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
						.innsyn(VISES_MANUELT_GODKJENT)
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isFalse();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato og hvis innsyn er BRUK_STANDARDREGLER.
	@Test
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdatoAndInnsynWithBrukStandardRegler() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, BRUK_STANDARDREGLER)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
						.innsyn(BRUK_STANDARDREGLER)
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Journalført før innsynsdato og hvis innsyn er null.
	@Test
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdatoAndInnsynIsNull() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Opprettet før innsynsdato
	@Test
	void shouldReturnTrueWhenOpprettetBeforeInnsynsdatoAndNotStartedWithVISES() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, SKJULES_BRUKERS_ONSKE)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.journalfoertDato(LocalDateTime.of(2017, 5, 6, 0, 0))
						.datoOpprettet(LocalDateTime.of(2015, 5, 6, 0, 0))
						.innsyn(SKJULES_BRUKERS_ONSKE)
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost);
		assertThat(actual).isTrue();
	}

	//	1c - Bruker får kun se midlertidige og ferdigstilte journalposter
	@Test
	void shouldReturnFalseWhenNotJournalfoertOrMottatt() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null).journalstatus(Journalstatus.UNDER_ARBEID).build();
		boolean actual = utledTilgangService.isJournalpostFerdigstiltOrMidlertidig(journalpost);
		assertThat(actual).isFalse();
	}

	//	1d - Bruker får ikke se feilregistrerte journalposter
	@Test
	void shouldReturnTrueWhenFeilregistrert() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangSak(Journalpost.TilgangSak.builder()
								.feilregistrert(true)
								.build())
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
		Journalpost journalpost = baseMottattJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.datoOpprettet(LocalDateTime.now())
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
	// Journalført - med sakstilknytning
	@ParameterizedTest
	@ValueSource(strings = {TEMA_FARSKAP, TEMA_KONTROLL, TEMA_KONTROLL_ANMELDELSE, TEMA_ARBEIDSRAADGIVNING_SKJERMET, TEMA_ARBEIDSRAADGIVNING_PSYKOLOGTESTER})
	void shouldReturnFalseWhenJournalfoertAndSakTemaIkkeInnsynForBruker(String tema) {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, SKJULES_BRUKERS_ONSKE)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.datoOpprettet(LocalDateTime.now())
						.tema(tema)
						.tilgangSak(Journalpost.TilgangSak.builder()
								.aktoerId(AKTOER_ID)
								.fagsystem(ARKIVSAKSYSTEM_GOSYS)
								.feilregistrert(false)
								.tema(tema)
								.build())
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
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.datoOpprettet(LocalDateTime.now())
						.tema(tema)
						.build())
				.build();
		assertThat(utledTilgangService.isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)).isFalse();
	}


	//	1f - Bruker får ikke innsyn i journalposter som er begrenset ihht. GDPR
	@Test
	void shouldReturnFalseWhenBegrensetWithGdpr() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.skjerming(SkjermingType.POL)
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalpostNotGDPRRestricted(journalpost);
		assertThat(actual).isFalse();
	}

	//	1g - Bruker får ikke innsyn i notater (jp.type = N) med mindre det er et forvaltningsnotat
	@Test
	void shouldReturnTrueWhenForvaltningsnotat() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.journalposttype(Journalposttype.N)
				.dokumenter(List.of(DokumentInfo.builder()
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.kategori(FORVALTNINGSNOTAT.name())
								.build())
						.build()))
				.build();
		boolean actual = utledTilgangService.isJournalpostForvaltningsnotat(journalpost);
		assertThat(actual).isTrue();
	}

	//	1i) Bruker kan ikke få se journalposter som innsyn begynner med SKJULES_*
	@ParameterizedTest
	@ValueSource(strings = {"SKJULES_INNSKRENKET_PARTSINNSYN", "SKJULES_ORGAN_INTERNT", "SKJULES_FEILSENDT", "SKJULES_BRUKERS_ONSKE"})
	void shouldReturnTrueWhenJournalpostInnsynSkjult(String innsyn) {
		Journalpost journalpost = getBaseJournalfoertJournalpostWithInnsyn(innsyn);
		boolean actual = utledTilgangService.isJournalpostInnsynSkjult(journalpost.getTilgang());
		assertThat(actual).isTrue();
	}

	//	1i)  Bruker kan få se journalposter Hvis innsyn begynner ikke med SKJULES_*
	@ParameterizedTest
	@ValueSource(strings = {"VISES_FORVALTNINGSNOTAT", "VISES_MANUELT_GODKJENT", "VISES_MASKINELT_GODKJENT", "BRUK_STANDARDREGLER"})
	void shouldReturnFalseWhenJournalpostInnsynErIKkeSkjult(String innsyn) {

		Journalpost journalpost = getBaseJournalfoertJournalpostWithInnsyn(innsyn);
		boolean actual = utledTilgangService.isJournalpostInnsynSkjult(journalpost.getTilgang());
		assertThat(actual).isFalse();
	}

	//	2a - Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	@Test
	void shouldReturnFalseWhenAvsenderMottakerIdIsNull() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(baseTilgangJournalpost(TEMA_DAGPENGER, null).avsenderMottakerId(null).build())
				.build();
		boolean actual = utledTilgangService.isAvsenderMottakerPart(journalpost, defaultBrukerIdenter().getIdenter());
		assertThat(actual).isFalse();
	}

	//	2a - Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	@Test
	void shouldReturnFalseWhenAvsenderMottakerIdIsAnnenPart() {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.tilgang(baseTilgangJournalpost(TEMA_DAGPENGER, null).avsenderMottakerId(ANNEN_PART).build())
				.build();
		boolean actual = utledTilgangService.isAvsenderMottakerPart(journalpost, defaultBrukerIdenter().getIdenter());
		assertThat(actual).isFalse();
	}

	//	2b - Bruker får ikke se skannede dokumenter
	@Test
	void shouldReturnTrueWhenSkannetDokument() {
		boolean imSkannetDokument = isSkannetDokument(SKAN_IM, SKJULES_BRUKERS_ONSKE);
		boolean netsSkannetDokument = isSkannetDokument(SKAN_NETS, SKJULES_INNSKRENKET_PARTSINNSYN);
		boolean penSkannetDokument = isSkannetDokument(SKAN_PEN, SKJULES_ORGAN_INTERNT);

		assertThat(imSkannetDokument).isTrue();
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
	void shouldReturnTrueWhenDokumentIsPolSkjermet() {
		boolean actual = utledTilgangService.isDokumentGDPRRestricted(Dokumentvariant.builder()
				.tilgangVariant(Dokumentvariant.TilgangVariant.builder()
						.skjerming(SkjermingType.POL)
						.build())
				.build());
		assertThat(actual).isTrue();
	}

	//	2f - Kasserte dokumenter skal ikke vises
	@Test
	void shouldReturnWhenDokumentIsKassert() {
		boolean actual = utledTilgangService.isDokumentKassert(DokumentInfo.builder().tilgangDokument(
						DokumentInfo.TilgangDokument.builder()
								.kassert(true)
								.build())
				.build());
		assertThat(actual).isTrue();
	}

	private boolean isSkannetDokument(final Kanal kanal, Innsyn innsyn) {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.kanal(kanal)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.mottakskanal(kanal)
						.innsyn(innsyn)
						.build())
				.build();
		return utledTilgangService.isSkannetDokumentAndInnsynIsNotVises(journalpost);
	}

	private void assertSkannetDokumentLokalUtskrift(final Kanal kanal, Innsyn innsyn) {
		Journalpost journalpost = baseJournalfoertJournalpost(TEMA_DAGPENGER, null)
				.journalposttype(Journalposttype.U)
				.kanal(kanal)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.mottakskanal(kanal)
						.innsyn(innsyn)
						.build())
				.build();
		boolean ironMountainActual = utledTilgangService.isSkannetDokumentAndInnsynIsNotVises(journalpost);
		assertThat(ironMountainActual).isTrue();
	}

	private Journalpost getBaseJournalfoertJournalpostWithInnsyn(String innsyn) {
		return baseJournalfoertJournalpost(TEMA_DAGPENGER, valueOf(innsyn))
				.dokumenter(List.of(DokumentInfo.builder()
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.build())
						.build(), DokumentInfo.builder()
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.build())
						.build()))
				.build();
	}

}