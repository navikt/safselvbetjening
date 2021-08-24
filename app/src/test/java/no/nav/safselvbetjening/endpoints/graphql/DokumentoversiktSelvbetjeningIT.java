package no.nav.safselvbetjening.endpoints.graphql;

import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.graphql.ErrorCode;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokumentoversiktSelvbetjeningIT extends AbstractItest {
	private static final String BRUKER_ID = "12345678911";

	@Test
	void shouldGetDokumentoversiktWhenAllQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertTemaQuery(dokumentoversikt);
		assertFagsakQuery(dokumentoversikt);
		assertJournalposterQuery(dokumentoversikt);
	}

	@Test
	void shouldGetDokumentoversiktWhenSubClaimUsed() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversiktSubToken("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertTemaQuery(dokumentoversikt);
		assertFagsakQuery(dokumentoversikt);
		assertJournalposterQuery(dokumentoversikt);
	}

	@Test
	void shouldGetDokumentoversiktWhenTemaQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_tema.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertTemaQuery(dokumentoversikt);
	}

	@Test
	void shouldGetDokumentoversiktWhenFagsakQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_fagsak.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertFagsakQuery(dokumentoversikt);
	}

	@Test
	void shouldGetDokumentoversiktWhenJournalposterQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_journalposter.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertJournalposterQuery(dokumentoversikt);
	}

	private void assertTemaQuery(Dokumentoversikt dokumentoversikt) {
		assertThat(dokumentoversikt.getTema()).hasSize(2);
		Sakstema foreldrepenger = dokumentoversikt.getTema().get(0);
		assertThat(foreldrepenger.getKode()).isEqualTo("FOR");
		assertThat(foreldrepenger.getNavn()).isEqualTo("Foreldre- og svangerskapspenger");
		assertThat(foreldrepenger.getJournalposter()).hasSize(2);
		Sakstema pensjon = dokumentoversikt.getTema().get(1);
		assertThat(pensjon.getKode()).isEqualTo("UFO");
		assertThat(pensjon.getNavn()).isEqualTo("Uføretrygd");
		assertThat(pensjon.getJournalposter()).hasSize(1);
	}

	private void assertFagsakQuery(Dokumentoversikt dokumentoversikt) {
		assertThat(dokumentoversikt.getFagsak()).hasSize(2);
		Fagsak foreldrepenger = dokumentoversikt.getFagsak().get(0);
		assertThat(foreldrepenger.getFagsakId()).isEqualTo("fp-12345");
		assertThat(foreldrepenger.getFagsaksystem()).isEqualTo("FS38");
		assertThat(foreldrepenger.getTema()).isEqualTo("FOR");
		assertThat(foreldrepenger.getJournalposter()).hasSize(1);
		Fagsak pensjon = dokumentoversikt.getFagsak().get(1);
		assertThat(pensjon.getTema()).isEqualTo("UFO");
		assertThat(pensjon.getFagsakId()).isEqualTo("21998969");
		assertThat(pensjon.getFagsaksystem()).isEqualTo("PP01");
		assertThat(pensjon.getJournalposter()).hasSize(1);
	}

	private void assertJournalposterQuery(Dokumentoversikt dokumentoversikt) {
		assertThat(dokumentoversikt.getJournalposter()).hasSize(3);
	}

	@Test
	void shouldGetDokumentoversiktWhenOnlyForeldrepengerTemaQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_for.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(data.getTema()).hasSize(1);
		Sakstema foreldrepenger = data.getTema().get(0);
		assertThat(foreldrepenger.getJournalposter()).hasSize(2);
	}

	@Test
	void shouldGetPartialDokumentoversiktWhenPensjonSakFails() throws Exception {
		happyStubs();
		stubPensjonSak("pensjonsak_error.xml");
		stubFagarkiv("finnjournalposter_missing_pen.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(data.getTema()).hasSize(1);
		Sakstema foreldrepenger = data.getTema().get(0);
		assertThat(foreldrepenger.getJournalposter()).hasSize(2);
	}

	@Test
	void shouldNotCallFagarkivWhenTemaOnlyQuery() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_tema_only.query");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(data.getTema()).hasSize(2);
		verify(0, postRequestedFor(urlEqualTo("/fagarkiv")));
	}

	@Test
	void shouldReturnEmptyListWhenIngenSakerAndJournalposterOnBruker() throws Exception {
		happyStubs();
		stubSak("saker_empty.json");
		stubFagarkiv("finnjournalposter_empty.json");
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_for.query");

		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(data.getTema()).hasSize(0);
	}

	@Test
	void shouldReturnNotFoundWhenIngenBrukerIdenter() throws Exception {
		happyStubs();
		stubPdl("pdl_not_found.json");
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_for.query");

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.NOT_FOUND.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdent() throws Exception {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders("22222222222"), HttpMethod.POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnBadRequestWhenQueryIdentInvalid() throws Exception {
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_invalid_ident.query");

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.BAD_REQUEST.getText());
	}

	@Test
	void shouldReturnBadRequestWhenQueryIdentBlank() throws Exception {
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_blank_ident.query");

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.BAD_REQUEST.getText());
	}

	private void happyStubs() {
		stubAzure();
		stubPdl();
		stubSak();
		stubPensjonSak();
		stubFagarkiv();
	}

	private ResponseEntity<GraphQLResponse> callDokumentoversikt(final String queryfile) throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(BRUKER_ID), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentoversiktSubToken(final String queryfile) throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeadersSubToken(BRUKER_ID), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}
}
