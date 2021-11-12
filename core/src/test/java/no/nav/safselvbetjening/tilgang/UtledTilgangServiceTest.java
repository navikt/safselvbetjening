package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_IM;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_NETS;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_PEN;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_AKTOER_ID;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ARKIVSAKSYSTEM_GOSYS;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.ARKIVSAKSYSTEM_PENSJON;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.IDENT;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_DAGPENGER;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_FAR;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_KONTROLL;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.TEMA_PENSJON;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseJournalfoertJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.baseMottattJournalpost;
import static no.nav.safselvbetjening.tilgang.UtledTilgangTestObjects.defaultBrukerIdenter;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class UtledTilgangServiceTest {

	private final UtledTilgangService utledTilgangService;

	public UtledTilgangServiceTest() {
		SafSelvbetjeningProperties safSelvbetjeningProperties = new SafSelvbetjeningProperties();
		safSelvbetjeningProperties.setTidligstInnsynDato(LocalDate.of(2016, 6, 4));
		utledTilgangService = new UtledTilgangService(safSelvbetjeningProperties);
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
		Journalpost journalpost = baseJournalfoertJournalpost().build();
		boolean brukerPart = utledTilgangService.isBrukerPart(journalpost, defaultBrukerIdenter());
		assertThat(brukerPart).isTrue();
	}

	//	1a - Bruker må være part for å se journalposter
	// 	Journalført - har sakstilknytning og bruker i pensjon (PEN)
	@Test
	void shouldReturnTrueWhenJournalfoertInPensjonAndBrukerPart() {
		Journalpost journalpost = baseJournalfoertJournalpost()
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
		Journalpost journalpost = baseJournalfoertJournalpost()
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
		Journalpost journalpost = baseJournalfoertJournalpost()
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
	void shouldReturnTrueWhenJournalfoertBeforeInnsynsdato() {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.journalfoertDato(LocalDateTime.of(2016, 5, 6, 0, 0))
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdato(journalpost);
		assertThat(actual).isTrue();
	}

	//	1b - Bruker får ikke se journalposter som er opprettet før 04.06.2016
	// Opprettet før innsynsdato
	@Test
	void shouldReturnTrueWhenOpprettetBeforeInnsynsdato() {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.journalfoertDato(LocalDateTime.of(2017, 5, 6, 0, 0))
						.datoOpprettet(LocalDateTime.of(2015, 5, 6, 0, 0))
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdato(journalpost);
		assertThat(actual).isTrue();
	}

	//	1c - Bruker får kun se midlertidige og ferdigstilte journalposter
	@Test
	void shouldReturnFalseWhenNotJournalfoertOrMottatt() {
		Journalpost journalpost = baseJournalfoertJournalpost().journalstatus(Journalstatus.UNDER_ARBEID).build();
		boolean actual = utledTilgangService.isJournalpostFerdigstiltOrMidlertidig(journalpost);
		assertThat(actual).isFalse();
	}

	//	1d - Bruker får ikke se feilregistrerte journalposter
	@Test
	void shouldReturnTrueWhenFeilregistrert() {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.tilgangSak(Journalpost.TilgangSak.builder()
								.feilregistrert(true)
								.build())
						.build())
				.build();
		boolean actual = utledTilgangService.isJournalpostFeilregistrert(journalpost);
		assertThat(actual).isTrue();
	}

	//	1e - Bruker får ikke innsyn i kontroll- eller farskapssaker
	// Mottatt - ingen sakstilknytning
	@Test
	void shouldReturnFalseWhenMottattAndKontrollsakOrFarskapssak() {
		assertThat(getTilgangWhenMottattAndKontrollsak(TEMA_FAR)).isFalse();
		assertThat(getTilgangWhenMottattAndKontrollsak(TEMA_KONTROLL)).isFalse();
	}

	//	1e - Bruker får ikke innsyn i kontroll- eller farskapssaker
	// Journalført - med sakstilknytning
	@Test
	void shouldReturnFalseWhenJournalfoertAndKontrollOrFarskapssakWithSak() {
		assertThat(getTilgangWhenJournalfoertWithSak(TEMA_FAR)).isFalse();
		assertThat(getTilgangWhenJournalfoertWithSak(TEMA_KONTROLL)).isFalse();
	}

	//	1e - Bruker får ikke innsyn i kontroll- eller farskapssaker
	// Journalført - uten sakstilknytning
	@Test
	void shouldReturnFalseWhenJournalfoertAndKontrollOrFarskapssakWithoutSak() {
		assertThat(getTilgangWhenJournalfoertWithoutSak(TEMA_FAR)).isFalse();
		assertThat(getTilgangWhenJournalfoertWithoutSak(TEMA_KONTROLL)).isFalse();
	}


	//	1f - Bruker får ikke innsyn i journalposter som er begrenset ihht. GDPR
	@Test
	void shouldReturnFalseWhenBegrensetWithGdpr() {
		Journalpost journalpost = baseJournalfoertJournalpost()
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
		Journalpost journalpost = baseJournalfoertJournalpost()
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

	//	1h - Bruker får ikke innsyn journalposter der et eller flere dokumenter markert som organinternt
	@Test
	void shouldReturnFalseWhenAtleastOneDokumentIsOrganintern() {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.dokumenter(List.of(DokumentInfo.builder()
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.organinternt(false)
								.build())
						.build(), DokumentInfo.builder()
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.organinternt(true)
								.build())
						.build()))
				.build();
		boolean actual = utledTilgangService.isJournalpostNotOrganInternt(journalpost);
		assertThat(actual).isFalse();
	}

	//	2a - Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	@Test
	void shouldReturnTrueWhenAvsenderMottakerIdIsAnnenPart() {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder().avsenderMottakerId(ANNEN_PART).build())
				.build();
		boolean actual = utledTilgangService.isAvsenderMottakerNotPart(journalpost, defaultBrukerIdenter().getIdenter());
		assertThat(actual).isTrue();
	}

	//	2b - Bruker får ikke se skannede dokumenter
	@Test
	void shouldReturnTrueWhenSkannetDokument() {
		assertSkannetDokument(SKAN_IM);
		assertSkannetDokument(SKAN_NETS);
		assertSkannetDokument(SKAN_PEN);
	}

	//	2b - Bruker får ikke se skannede dokumenter - lokal utskrift - journalposttype U - mottakskanal satt
	@Test
	void shouldReturnTrueWhenSkannetDokumentAndLokalUtskrift() {
		assertSkannetDokumentLokalUtskrift(SKAN_IM);
		assertSkannetDokumentLokalUtskrift(SKAN_NETS);
		assertSkannetDokumentLokalUtskrift(SKAN_PEN);
	}

	//	2d - Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	@Test
	void shouldReturnTrueWhenDokumentIsInnskrenketpartsinnsyn() {
		boolean actual = utledTilgangService.isDokumentInnskrenketPartsinnsyn(DokumentInfo.builder()
				.tilgangDokument(DokumentInfo.TilgangDokument.builder()
						.innskrenketPartsinnsyn(true)
						.build())
				.build());
		assertThat(actual).isTrue();
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

	private void assertSkannetDokument(final Kanal kanal) {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.kanal(kanal)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.mottakskanal(kanal)
						.build())
				.build();
		boolean ironMountainActual = utledTilgangService.isSkannetDokument(journalpost);
		assertThat(ironMountainActual).isTrue();
	}

	private void assertSkannetDokumentLokalUtskrift(final Kanal kanal) {
		Journalpost journalpost = baseJournalfoertJournalpost()
				.journalposttype(Journalposttype.U)
				.kanal(kanal)
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.mottakskanal(kanal)
						.build())
				.build();
		boolean ironMountainActual = utledTilgangService.isSkannetDokument(journalpost);
		assertThat(ironMountainActual).isTrue();
	}

	boolean getTilgangWhenJournalfoertWithoutSak(String tema){
		Journalpost journalpost = baseJournalfoertJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.datoOpprettet(LocalDateTime.now())
						.tema(tema)
						.build())
				.build();
		return utledTilgangService.isJournalpostNotKontrollsakOrFarskapssak(journalpost);
	}

	boolean getTilgangWhenJournalfoertWithSak(String tema){
		Journalpost journalpost = baseJournalfoertJournalpost()
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
		return utledTilgangService.isJournalpostNotKontrollsakOrFarskapssak(journalpost);

	}

	boolean getTilgangWhenMottattAndKontrollsak(String tema) {
		Journalpost journalpost = baseMottattJournalpost()
				.tilgang(Journalpost.TilgangJournalpost.builder()
						.datoOpprettet(LocalDateTime.now())
						.tema(tema)
						.build())
				.build();
		return utledTilgangService.isJournalpostNotKontrollsakOrFarskapssak(journalpost);
	}
}