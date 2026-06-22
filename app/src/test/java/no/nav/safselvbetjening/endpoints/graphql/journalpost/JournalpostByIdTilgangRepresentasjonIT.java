package no.nav.safselvbetjening.endpoints.graphql.journalpost;

import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class JournalpostByIdTilgangRepresentasjonIT extends AbstractJournalpostItest {

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		stubTokenx();
	}

	/// Hvis pålogget bruker er 22222222222 (A) og journalposten tilhører 12345678911 (B) så skal man undersøke om bruker A har representasjon overfor bruker B
	/// Hvis repr-api returnerer vergemål og fullmakt for A der B er både vergehaver og fullmektig.
	/// Der kun vergemål tema matcher tema journalposten gjelder så skal journalpost hentes
	@Test
	void skalHenteJournalpostHvisPaaloggetBrukerErVergeOgFullmektigDerKunVergemaalHarGyldigTema() {
		stubReprApiRepresentasjon("repr-api-representasjon-vergemaal-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsRepresentant();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertInngaaendeJournalpost(journalpost);

		assertDokumenter(journalpost.getDokumenter());
	}

	/// Hvis pålogget bruker er 22222222222 (A) og journalposten tilhører 12345678911 (B) så skal man undersøke om bruker A har representasjon overfor bruker B
	/// Hvis repr-api returnerer vergemål og fullmakt for A der B er både vergehaver og fullmektig.
	/// Der kun fullmakt tema matcher tema journalposten gjelder så skal journalpost hentes
	@Test
	void skalHenteJournalpostHvisPaaloggetBrukerErVergeOgFullmektigDerKunFullmaktHarGyldigTema() {
		stubReprApiRepresentasjon("repr-api-representasjon-fullmakt-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostByIdAsRepresentant();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertInngaaendeJournalpost(journalpost);

		assertDokumenter(journalpost.getDokumenter());
	}

}
