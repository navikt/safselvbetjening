package no.nav.safselvbetjening.endpoints.graphql;

import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

public class JournalpostByIdIT extends AbstractItest {
	private static final String BRUKER_ID = "12345678911";

	@Test
	void shouldGetJournalpostById() throws Exception {
		ResponseEntity<GraphQLResponse> response = callJournalpostById("journalpost_by_id_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpostById = graphQLResponse.getData().getJournalpostById();
		assertThat(journalpostById.getJournalpostId()).isEqualTo("1000000");
	}

	private ResponseEntity<GraphQLResponse> callJournalpostById(final String queryfile) throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, Map.of("journalpostId", "1000000"));
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(BRUKER_ID), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}
}
