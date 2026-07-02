package no.nav.safselvbetjening.endpoints.graphql.dokumentoversiktselvbetjening;

import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static java.util.Objects.requireNonNull;
import static no.nav.safselvbetjening.graphql.ErrorCode.NOT_FOUND;
import static no.nav.safselvbetjening.graphql.ErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

public class DokumentoversiktSelvbetjeningFullmaktIT extends AbstractDokumentoversiktSelvbetjeningItest {

	@Test
	void shouldGetDokumentoversiktWhenTokenNotMatchingQueryIdentAndFullmaktExistsForTema() throws Exception {
		happyStubs("finnjournalposter_happy_gsak.json");
		stubReprApiRepresentasjon("repr-api-fullmakt-for.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(dokumentoversikt.getTema()).hasSize(1);
		// fullmakt FOR
		assertThat(dokumentoversikt.getTema().getFirst().getKode()).isEqualTo("FOR");
		assertThat(dokumentoversikt.getFagsak().getFirst().getTema()).isEqualTo("FOR");
		assertThat(dokumentoversikt.getJournalposter().getFirst().getTema()).isEqualTo("FOR");
	}

	@Test
	void shouldGetDokumentoversiktWhenTokenNotMatchingQueryIdentAndFullmaktExistsForTemaOnlyTemaInArguments() throws Exception {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap.json");
		stubSak("saker_happy_for_aap.json");
		stubReprApiRepresentasjon("repr-api-fullmakt-for-aap.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_for.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(1);
		// fullmakt FOR,AAP men kun tema FOR i filter
		assertThat(data.getTema().getFirst().getKode()).isEqualTo("FOR");
		assertThat(data.getTema().getFirst().getJournalposter()).hasSize(1);
	}

	@Test
	void shouldGetEmptyDokumentoversiktWhenTokenNotMatchingQueryIdentAndFullmaktDoesNotCoverTemaArgumentInQuery() throws Exception {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_bar.json");
		stubReprApiRepresentasjon("repr-api-fullmakt-for-aap.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_bar.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(0);
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndWrongFullmakt() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon("repr-api-fullmakt-feil-bruker.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturns4xx() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon(HttpStatus.FORBIDDEN);

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturns5xx() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon(HttpStatus.INTERNAL_SERVER_ERROR);

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturnsInvalidJson() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon("repr-api-fullmakt-invalid.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturnsInvalidJsonNoArray() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon("repr-api-invalid-no-array.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnNotFoundWhenIngenBrukerIdenter() throws Exception {
		happyStubs();
		stubPdl("pdl_not_found.json");
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_for.query");

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(NOT_FOUND.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndNoFullmakt() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon();
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndIngenFullmaktOmraader() throws Exception {
		stubTokenx();
		stubReprApiRepresentasjon("repr-api-empty.json");
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}
}
