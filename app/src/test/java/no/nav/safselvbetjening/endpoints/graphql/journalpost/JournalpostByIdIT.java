package no.nav.safselvbetjening.endpoints.graphql.journalpost;

import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import no.nav.safselvbetjening.graphql.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.Objects.requireNonNull;
import static no.nav.safselvbetjening.domain.Tema.UFO;
import static no.nav.safselvbetjening.graphql.ErrorCode.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class JournalpostByIdIT extends AbstractJournalpostItest {

	/**
	 * Alt i orden
	 */
	@Test
	void skalQueryInngaaendeJournalpostById() {
		stubPdlGenerell();
		stubDokarkivJournalpost();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertInngaaendeJournalpost(journalpost);

		assertDokumenter(journalpost.getDokumenter());
	}
	/**
	 * Alt i orden
	 */
	@Test
	void skalQueryUtgaaendeJournalpostById() {
		stubPdlGenerell();
		stubDokarkivJournalpost("1c-journalpost-ok_utgaaende.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertUtgaaendeJournalpost(journalpost);

		assertDokumenter(journalpost.getDokumenter());
	}

	/**
	 * Hvis journalpost er knyttet til en pensjon sak (fagsystem=PEN) så skal bruker utledes fra pensjon sakId.
	 * Journalpost returneres hvis bruker på pensjon saken matcher bruker som henter journalposten
	 * Tema på journalposten skal også være tema knyttet til pensjon saken. Kan være Uføretrygd (UFO) eller Alderspensjon (PEN)
	 */
	@Test
	void skalQueryJournalpostHvisTilknyttetPensjonSak() {
		stubPdlGenerell();
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		stubDokarkivJournalpost("1c-journalpost-pensjon-ok.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertThat(journalpost.getTema()).isEqualTo(UFO.name());
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo("2000000");
	}

	/**
	 * Alt i orden
	 */
	@Test
	void skalQueryJournalpostByIdWithRekkefoelge() {
		stubPdlGenerell();
		stubDokarkivJournalpost("1c-journalpost-rekkefoelge-vedlegg-ok.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertInngaaendeJournalpost(journalpost);
		assertDokumenterInRekkefoelge(journalpost.getDokumenter());
	}

	/**
	 * Hvis journalpostId ikke er siffer eller er blank så skal det returneres errors med extensions.code = bad_request
	 */
	@ParameterizedTest
	@ValueSource(strings = {"40000000a", ""})
	void skalGiBadRequestCodeHvisJournalpostIdUgyldig(String journalpostId) {
		ResponseEntity<GraphQLResponse> response = queryJournalpostById("journalpost_by_id_all.query", BRUKER_ID, journalpostId);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(BAD_REQUEST.getText());
	}

	/**
	 * Hvis journalpostId ikke finnes i fagarkivet så skal det returneres errors med extension.code = not_found
	 */
	@Test
	void skalGiNotFoundCodeHvisJournalpostIkkeFinnes() {
		stubDokarkivJournalpost(NOT_FOUND);

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.NOT_FOUND.getText());
	}

	/**
	 * Hvis journalpostId fra journalpost ikke matcher journalpostId i argument, så skal det returneres errors med extension.code = server_error
	 */
	@Test
	void skalGiServerErrorCodeHvisJournalpostIdFraResponsIkkeMatcherArgument() {
		stubDokarkivJournalpost("1c-journalpost-feil-journalpost.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.SERVER_ERROR.getText());
	}

	/**
	 * Hvis journalpost er knyttet til en pensjon sak (fagsystem=PEN) så skal bruker utledes fra pensjon sakId.
	 * Hvis man ikke finner bruker for pensjon sakId i pensjon så returneres en Not Found feil.
	 */
	@Test
	void skalGiNotFoundFeilHvisBrukerForSakIdIkkeFinnesIPensjon() {
		stubPdlGenerell();
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-empty.json");
		stubDokarkivJournalpost("1c-journalpost-pensjon-ok.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.NOT_FOUND.getText());
		verify(1, getRequestedFor(urlEqualTo(HENT_BRUKER_FOR_PENSJONSSAK_PATH)));
	}

}
