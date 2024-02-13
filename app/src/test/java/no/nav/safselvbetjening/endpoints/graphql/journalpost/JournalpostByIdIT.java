package no.nav.safselvbetjening.endpoints.graphql.journalpost;

import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class JournalpostByIdIT extends AbstractJournalpostItest {

	@Test
	void shouldQueryJournalpostById() {
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
}
