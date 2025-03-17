package no.nav.safselvbetjening.endpoints.graphql.journalpost;

import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalstatus.JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

/**
 * Tester fullmakt-relaterte tilgangsregler implementasjon definert i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
 */
public class JournalpostByIdTilgangFullmaktIT extends AbstractJournalpostItest {

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		stubTokenx();
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten matcher tema dokumentet gjelder så skal dokument hentes
	 */
	@Test
	void skalFinneJournalpostByIdHvisPaaloggetBrukerErFullmektigMedGyldigFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertInngaaendeJournalpost(journalpost);

		assertDokumenter(journalpost.getDokumenter());
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten matcher tema dokumentet gjelder så skal dokument hentes
	 */
	@Test
	void skalFinneUtgaaendeNavNoJournalpostByIdHvisPaaloggetBrukerErFullmektigMedGyldigFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-hje.json");
		stubDokarkivJournalpost("1c-journalpost-ok_utgaaende.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertUtgaaendeJournalpost(journalpost);

		assertDokumenter(journalpost.getDokumenter());
	}


	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis dokumentet er knyttet til pensjon sak så skal man hente bruker og sak fra pensjon. I saken fra pensjon vil riktig tema stå PEN (Alderspensjon) eller UFO (Uføretrygd)
	 * Hvis pdl-fullmakt returnerer fullmakt for A der B er fullmaktsgiver og tema i fullmakten matcher tema dokumentet gjelder så skal dokument hentes
	 */
	@Test
	void skalFinneJournalpostByIdHvisDokumentTilknyttetPensjonSakHarTemaMedGyldigFullmakt() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-ufo.json");
		stubDokarkivJournalpost("1c-journalpost-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getTema()).isEqualTo("UFO");
		assertThat(journalpost.getJournalstatus()).isEqualTo(JOURNALFOERT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);
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
		stubDokarkivJournalpost("1c-journalpost-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		// UFO pensjonssak
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		// PEN journalpost
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
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

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
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

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
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

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en 4xx feil så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnerer4xxFeil() {
		stubReprApiFullmakt(FORBIDDEN);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en 5xx feil så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnerer5xxFeil() {
		stubReprApiFullmakt(INTERNAL_SERVER_ERROR);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en ugyldig JSON så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnererUgyldigJson() {
		stubReprApiFullmakt("repr-api-fullmakt-invalid.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en JSON uten array så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnererJsonUtenArray() {
		stubReprApiFullmakt("repr-api-fullmakt-invalid-no-array.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en fullmakt uten tema så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisReprApiReturnererJsonUtenTema() {
		stubReprApiFullmakt("repr-api-fullmakt-ingen-tema.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		assertGraphQlForbiddenError(response, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	/**
	 * Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	 * Hvis pdl-fullmakt returnerer en fullmakt som dekker kun journalpostens tema og ikke sakens tema så skal det returneres en Forbidden feil
	 * Grunnen til dette er at tema på journalpost metadata og sak metadata ikke er synkronisert. Så disse temaene kan være forskjellig.
	 */
	@Test
	void skalGiForbiddenFeilHvisFullmaktDekkerJournalpostTemaOgIkkeSakTema() {
		stubReprApiFullmakt("repr-api-fullmakt-tema-hje.json");
		stubDokarkivJournalpost("ukj-journalpost-tema-forskjellig-fra-fullmakt.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsFullmektig();

		assertGraphQlForbiddenError(response, FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}

	private static void assertGraphQlForbiddenError(ResponseEntity<GraphQLResponse> response, String feilmelding) {
		List<GraphQLResponse.Error> errors = response.getBody().getErrors();
		assertThat(errors).isNotNull();
		assertThat(errors).extracting(GraphQLResponse.Error::getMessage).contains(feilmelding);
		assertThat(errors).extracting(GraphQLResponse.Error::getExtensions)
				.extracting(GraphQLResponse.Extensions::getCode)
				.contains("forbidden");
	}
}
