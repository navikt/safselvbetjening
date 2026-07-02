package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_VERGEMAAL_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.hentdokument.HentDokumentService.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.hentdokument.HentDokumentService.DENY_REASON_VERGEMAAL_GJELDER_IKKE_FOR_TEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Tester vergemål-relaterte tilgangsregler implementasjon definert i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
 */
public class HentDokumentTilgangVergemaalIT extends AbstractHentDokumentItest {

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		stubTokenx();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer vergemål for A der B er vergehaver og tema i vergemålet matcher tema dokumentet gjelder så skal dokument hentes
	 * <p>
	 * Hvis dokumentet er et inngående hoveddokument med kanal NAV_NO skal det ikke genereres HoveddokumentLest hendelse
	 */
	@Test
	void skalHenteDokumentHvisPaaloggetBrukerErVergeMedGyldigVergemaal() {
		stubReprApiRepresentasjon("repr-api-vergemaal-tema-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer vergemål for A der B er vergehaver og tema i vergemålet matcher tema dokumentet gjelder så skal dokument hentes
	 * <p>
	 * Hvis dokumentet er et utgående hoveddokument med kanal NAV_NO skal det ikke genereres HoveddokumentLest hendelse
	 */
	@Test
	void skalHenteUtgaaendeNavNoDokumentOgIkkeSendeHoveddokumentLestHendelseHvisPaaloggetBrukerErVergeMedGyldigVergemaal() {
		stubReprApiRepresentasjon("repr-api-vergemaal-tema-hje.json");
		stubDokarkivJournalpost("1c-hentdokument-utgaaende-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis dokumentet er knyttet til pensjon sak så skal man hente bruker og sak fra pensjon. I saken fra pensjon vil riktig tema stå PEN (Alderspensjon) eller UFO (Uføretrygd)
	 * Hvis repr-api returnerer vergemål for A der B er vergehaver og tema i vergemålet matcher tema dokumentet gjelder så skal dokument hentes
	 */
	@Test
	void skalHenteDokumentHvisDokumentTilknyttetPensjonSakHarTemaMedGyldigVergemaal() {
		stubReprApiRepresentasjon("repr-api-vergemaal-tema-ufo.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis dokumentet er knyttet til pensjon sak så skal man hente bruker og sak fra pensjon. I saken fra pensjon vil riktig tema stå PEN (Alderspensjon) eller UFO (Uføretrygd)
	 * <p>
	 * Noen ganger så er tema på journalposten (PEN) forskjellig fra tema på pensjon saken (UFO). Da er det tema på pensjon saken som vergemålet skal dekke
	 * Hvis repr-api returnerer vergemål for A der B er vergehaver og tema i vergemålet ikke dekker tema på pensjon saken så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisVergemaalDekkerJournalpostTemaOgIkkePensjonssakTema() {
		stubReprApiRepresentasjon("repr-api-vergemaal-tema-pen.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		// UFO pensjonssak
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		// PEN journalpost
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_VERGEMAAL_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_VERGEMAAL_GJELDER_IKKE_FOR_TEMA);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer vergemål for A der B er vergehaver og tema i vergemålet ikke matcher tema dokumentet gjelder, så skal en Forbidden feil returneres
	 */
	@Test
	void skalGiForbiddenFeilHvisVergemaalIkkeDekkerTemaDokumentetGjelder() {
		stubReprApiRepresentasjon("repr-api-vergemaal-tema-pen.json");
		// tema HJE fra dokarkiv
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_VERGEMAAL_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_VERGEMAAL_GJELDER_IKKE_FOR_TEMA);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api ikke returnerer vergemål så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisVergemaalIkkeFinnes() {
		stubReprApiRepresentasjon("repr-api-vergemaal-empty.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer vergemål for en annen bruker C, selv om tema er dekkende, så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisVergemaalGjelderEnAnnenBrukerEnnDetDokumentetGjelder() {
		stubPdlGenerell();
		stubReprApiRepresentasjon("repr-api-vergemaal-feil-bruker.json");
		stubDokarkivJournalpost();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer en 4xx feil så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnerer4xxFeil() {
		stubReprApiRepresentasjon(FORBIDDEN);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer en 5xx feil så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnerer5xxFeil() {
		stubReprApiRepresentasjon(INTERNAL_SERVER_ERROR);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer en ugyldig JSON så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnererUgyldigJson() {
		stubReprApiRepresentasjon("repr-api-vergemaal-invalid.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer en JSON uten array så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnererJsonUtenArray() {
		stubReprApiRepresentasjon("repr-api-invalid-no-array.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer et vergemål uten tema så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnererJsonUtenTema() {
		stubReprApiRepresentasjon("repr-api-vergemaal-ingen-tema.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	 * Hvis repr-api returnerer et vergemål som dekker kun journalpostens tema og ikke sakens tema så skal det returneres en Forbidden feil
	 * Grunnen til dette er at tema på journalpost metadata og sak metadata ikke er synkronisert. Så disse temaene kan være forskjellig.
	 */
	@Test
	void skalGiForbiddenFeilHvisVergemaalDekkerJournalpostTemaOgIkkeSakTema() {
		stubReprApiRepresentasjon("repr-api-vergemaal-tema-hje.json");
		stubDokarkivJournalpost("ukj-hentdokument-journalpost-sak-forskjellig-tema-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_VERGEMAAL_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_VERGEMAAL_GJELDER_IKKE_FOR_TEMA);
	}
}
