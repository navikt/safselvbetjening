package no.nav.safselvbetjening.endpoints.graphql.dokumentoversiktselvbetjening;

import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static java.util.Objects.requireNonNull;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_TOKEN_MISMATCH_INGEN_REPRESENTASJON;
import static no.nav.safselvbetjening.graphql.ErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

/// Representasjon tilfeller som ikke kun gjelder den ene typen
public class DokumentoversiktSelvbetjeningRepresentasjonIT extends AbstractDokumentoversiktSelvbetjeningItest {

	@Test
	void shouldReturnDokumentoversiktUsingBothFullmaktAndVergemaalTemasWhenPresent() {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap_bar.json");
		stubSak("saker_happy_for_aap_bar.json");
		stubReprApiRepresentasjon("repr-api-representasjon-for-aap.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, URI.create("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(2);
		assertThat(data.getTema().getFirst().getKode()).isEqualTo("AAP");
		assertThat(data.getTema().getFirst().getJournalposter()).hasSize(1);
		assertThat(data.getTema().get(1).getKode()).isEqualTo("FOR");
		assertThat(data.getTema().get(1).getJournalposter()).hasSize(1);
		assertThat(data.getJournalposter()).hasSize(2);
		assertThat(data.getJournalposter()).extracting(Journalpost::getTema)
				.containsOnly("AAP", "FOR");
		assertThat(data.getFagsak()).hasSize(2);
		assertThat(data.getFagsak()).extracting(Fagsak::getTema)
				.containsOnly("AAP", "FOR");
		assertThat(data.getTema()).hasSize(2);
		assertThat(data.getTema().getFirst().getKode()).isEqualTo("AAP");
		assertThat(data.getTema().getFirst().getJournalposter()).hasSize(1);
		assertThat(data.getTema().get(1).getKode()).isEqualTo("FOR");
		assertThat(data.getTema().get(1).getJournalposter()).hasSize(1);
		assertThat(data.getJournalposter()).hasSize(2);
		assertThat(data.getJournalposter()).extracting(Journalpost::getTema)
				.containsOnly("AAP", "FOR");
		assertThat(data.getFagsak()).hasSize(2);
		assertThat(data.getFagsak()).extracting(Fagsak::getTema)
				.containsOnly("AAP", "FOR");
	}

	@Test
	void shouldReturnEmptyDokumentoversiktWhenNeitherFullmaktOrVergemaalCoversTema() {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap_bar.json");
		stubSak("saker_happy_for_aap_bar.json");
		stubReprApiRepresentasjon("repr-api-representasjon-ufo-dag.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, URI.create("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).isEmpty();
		assertThat(data.getJournalposter()).isEmpty();
		assertThat(data.getFagsak()).isEmpty();
	}

	@Test
	void shouldReturnDokumentoversiktWhenRepresentantHasTemaAccessButOnlyMatchingTemaInArgument() {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap_bar.json");
		stubSak("saker_happy_for_aap_bar.json");
		stubReprApiRepresentasjon("repr-api-representasjon-for-aap.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_for.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, URI.create("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(1);
		assertThat(data.getTema().getFirst().getKode()).isEqualTo("FOR");
		assertThat(data.getTema().getFirst().getJournalposter()).hasSize(1);
	}

	@Test
	void shouldReturnDokumentoversiktUsingRepresentasjonAndNotFailIfNewUnknownRepresentasjonIsAddedToReprApi() {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap_bar.json");
		stubSak("saker_happy_for_aap_bar.json");
		stubReprApiRepresentasjon("repr-api-representasjon-for-aap-foreldreansvar.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_journalposter.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, URI.create("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getJournalposter()).hasSize(2);
		assertThat(data.getJournalposter()).extracting(Journalpost::getTema)
				.containsOnly("AAP", "FOR");
	}

	@Test
	void shouldReturnDokumentoversiktWithUnauthorizedUsingRepresentasjonWhenReprApiReturnsNulls() {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap_bar.json");
		stubSak("saker_happy_for_aap_bar.json");
		stubReprApiRepresentasjon("repr-api-representasjon-nulls.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, URI.create("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode(), GraphQLResponse.Error::getMessage)
				.containsOnly(tuple(UNAUTHORIZED.getText(), FEILMELDING_TOKEN_MISMATCH_INGEN_REPRESENTASJON));
	}

	@Test
	void shouldReturnDokumentoversiktWithUnauthorizedWhenRepresentantDoesNotMatch() {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap_bar.json");
		stubSak("saker_happy_for_aap_bar.json");
		stubReprApiRepresentasjon("repr-api-representasjon-feil-representant.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(REPRESENTANT_ID), POST, URI.create("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode(), GraphQLResponse.Error::getMessage)
				.containsOnly(tuple(UNAUTHORIZED.getText(), FEILMELDING_TOKEN_MISMATCH_INGEN_REPRESENTASJON));
	}
}
