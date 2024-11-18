package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.hentdokument.HentDokumentService.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.hentdokument.HentDokumentService.DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Tester fullmakt-relaterte tilgangsregler implementasjon definert i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
 */
public class HentDokumentTilgangFullmaktIT extends AbstractHentDokumentItest {

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		stubTokenx();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten matcher tema dokumentet gjelder så skal dokument hentes
	 * <p>
	 * Hvis dokumentet er et inngående hoveddokument med kanal NAV_NO skal det ikke genereres HoveddokumentLest hendelse
	 */
	@Test
	void skalHenteDokumentHvisPaaloggetBrukerErFullmektigMedGyldigFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten matcher tema dokumentet gjelder så skal dokument hentes
	 * <p>
	 * Hvis dokumentet er et utgående hoveddokument med kanal NAV_NO skal det ikke genereres HoveddokumentLest hendelse
	 */
	@Test
	void skalHenteUtgaaendeNavNoDokumentOgIkkeSendeHoveddokumentLestHendelseHvisPaaloggetBrukerErFullmektigMedGyldigFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-hje.json");
		stubDokarkivJournalpost("1c-hentdokument-utgaaende-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}


	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis dokumentet er knyttet til pensjon sak så skal man hente bruker og sak fra pensjon. I saken fra pensjon vil riktig tema stå PEN (Alderspensjon) eller UFO (Uføretrygd)
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten matcher tema dokumentet gjelder så skal dokument hentes
	 */
	@Test
	void skalHenteDokumentHvisDokumentTilknyttetPensjonSakHarTemaMedGyldigFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-ufo.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis dokumentet er knyttet til pensjon sak så skal man hente bruker og sak fra pensjon. I saken fra pensjon vil riktig tema stå PEN (Alderspensjon) eller UFO (Uføretrygd)
	 * <p>
	 * Noen ganger så er tema på journalposten (PEN) forskjellig fra tema på pensjon saken (UFO). Da er det tema på pensjon saken som fullmakten skal dekke
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten ikke dekker tema på pensjon saken så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisFullmaktDekkerJournalpostTemaOgIkkePensjonssakTema() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-pen.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		// UFO pensjonssak
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		// PEN journalpost
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten ikke matcher tema dokumentet gjelder, så skal en Forbidden feil returneres
	 */
	@Test
	void skalGiForbiddenFeilHvisFullmaktIkkeDekkerTemaDokumentetGjelder() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-pen.json");
		// tema HJE fra dokarkiv
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt ikke returnerer fullmakt så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisFullmaktIkkeFinnes() {
		stubReprApiFullmakt("repr-api-fullmakt-empty.json");

		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer fullmakt for en annen bruker C, selv om tema er dekkende, så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisFullmaktGjelderEnAnnenBrukerEnnDetDokumentetGjelder() {
		stubPdlGenerell();
		stubReprApiFullmakt("repr-api-fullmakt-feil-bruker.json");
		stubDokarkivJournalpost();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en 4xx feil så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisPdlFullmaktReturnerer4xxFeil() {
		stubReprApiFullmakt(FORBIDDEN);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en 5xx feil så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisPdlFullmaktReturnerer5xxFeil() {
		stubReprApiFullmakt(INTERNAL_SERVER_ERROR);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en ugyldig JSON så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisPdlFullmaktReturnererUgyldigJson() {
		stubReprApiFullmakt("repr-api-fullmakt-invalid.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en JSON uten array så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisPdlFullmaktReturnererJsonUtenArray() {
		stubReprApiFullmakt("repr-api-fullmakt-invalid-no-array.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en fullmakt uten tema så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisPdlFullmaktReturnererJsonUtenTema() {
		stubReprApiFullmakt("repr-api-fullmakt-ingen-tema.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en fullmakt som dekker kun journalpostens tema og ikke sakens tema så skal det returneres en Forbidden feil
	 * Grunnen til dette er at tema på journalpost metadata og sak metadata ikke er synkronisert. Så disse temaene kan være forskjellig.
	 */
	@Test
	void skalGiForbiddenFeilHvisFullmaktDekkerJournalpostTemaOgIkkeSakTema() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-hje.json");
		stubDokarkivJournalpost("ukj-hentdokument-journalpost-sak-forskjellig-tema-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}
}
