package no.nav.safselvbetjening.endpoints.graphql;

import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Sakstype;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.Objects.requireNonNull;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Journalstatus.JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.Sakstype.GENERELL_SAK;
import static no.nav.safselvbetjening.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.safselvbetjening.graphql.ErrorCode.NOT_FOUND;
import static no.nav.safselvbetjening.graphql.ErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

public class DokumentoversiktSelvbetjeningIT extends AbstractItest {
	private static final String BRUKER_ID = "12345678911";
	private static final String BRUKER_NAVN = "HARRY POTTER";
	private static final String UKJENT_MOTTAKER = "Ukjent mottaker";
	private static final String UKJENT_AVSENDER = "Ukjent avsender";

	@Test
	void shouldGetDokumentoversiktWhenAllQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertTemaQuery(dokumentoversikt);
		assertFagsakQuery(dokumentoversikt);
		assertJournalposterQuery(dokumentoversikt.getJournalposter());
	}

	@Test
	void shouldGetDokumentoversiktWhenAllAndTemporaryJournalposterQueried() throws Exception {
		happyStubs("finnjournalposter_no_saksrelasjon.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(dokumentoversikt.getJournalposter()).hasSize(3);
	}

	@Test
	void shouldGetDokumentoversiktWhenSubClaimUsed() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversiktSubToken("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertTemaQuery(dokumentoversikt);
		assertFagsakQuery(dokumentoversikt);
		assertJournalposterQuery(dokumentoversikt.getJournalposter());
	}

	@Test
	void shouldGetFallbackNavnWhenAvsenderMottakerNavnIsNull() throws Exception {
		happyStubs("finnjournalposter_avsendermottaker_navn_null.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(dokumentoversikt.getJournalposter()).hasSize(3);

		assertTemaQuery(dokumentoversikt);
		assertFagsakQuery(dokumentoversikt);

		assertThat(dokumentoversikt.getJournalposter()).hasSize(3);
		dokumentoversikt.getJournalposter().sort(Comparator.comparing(Journalpost::getJournalpostId));
		List<Journalpost> journalposts = dokumentoversikt.getJournalposter();
		doAssertJournalpost(journalposts.get(0), "FOR", "SØKNAD_FORELDREPENGER_FØDSEL", "fp-12345", "FS38", FAGSAK, UKJENT_MOTTAKER);
		doAssertJournalpost(journalposts.get(1), "UFO", "Søknad om Uføretrygd", "21998969", "PP01", FAGSAK, UKJENT_MOTTAKER);
		doAssertJournalpost(journalposts.get(2), "FOR", "Bekreftelse fra Arbeidsgiver ifbm foreldrepenger", null, null, GENERELL_SAK, UKJENT_AVSENDER);
	}


	@Test
	void shouldGetDokumentoversiktWhenTemaQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_tema.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertTemaQuery(dokumentoversikt);

		List<Journalpost> journalposts = dokumentoversikt.getTema().getFirst().getJournalposter();
		assertThat(journalposts).hasSize(2);
		journalposts.sort(Comparator.comparing(Journalpost::getJournalpostId));
		doAssertJournalpost(journalposts.get(0), "FOR", "SØKNAD_FORELDREPENGER_FØDSEL", "fp-12345", "FS38", FAGSAK, BRUKER_NAVN);
		doAssertJournalpost(journalposts.get(1), "FOR", "Bekreftelse fra Arbeidsgiver ifbm foreldrepenger", null, null, GENERELL_SAK, BRUKER_NAVN);
	}

	@Test
	void shouldGetDokumentoversiktWhenFagsakQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_fagsak.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertFagsakQuery(dokumentoversikt);

		List<Journalpost> journalposts = dokumentoversikt.getFagsak().getFirst().getJournalposter();
		assertThat(journalposts).hasSize(1);
		journalposts.sort(Comparator.comparing(Journalpost::getJournalpostId));
		doAssertJournalpost(journalposts.get(0), "FOR", "SØKNAD_FORELDREPENGER_FØDSEL", "fp-12345", "FS38", FAGSAK, BRUKER_NAVN);
	}

	@Test
	void shouldGetDokumentoversiktWhenJournalposterQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_journalposter.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertJournalposterQuery(dokumentoversikt.getJournalposter());
	}

	@Test
	void shouldGetDokumentoversiktWhenJournalposterQueriedWithInnsynVises() throws Exception {
		happyStubWithInnsyn("finnjournalposter_innsyn_vises.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_journalposter.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertJournalposterQuery(dokumentoversikt.getJournalposter());
	}

	@Test
	void shouldGetDokumentoversiktWhenJournalposterQueriedWithInnsynSkjules() throws Exception {
		happyStubWithInnsyn("finnjournalposter_innsyn_skjules.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_journalposter.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt dokumentoversikt = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(dokumentoversikt.getJournalposter()).hasSize(0);
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

	private void assertJournalposterQuery(List<Journalpost> journalposts) {
		assertThat(journalposts).hasSize(3);
		journalposts.sort(Comparator.comparing(Journalpost::getJournalpostId));
		doAssertJournalpost(journalposts.get(0), "FOR", "SØKNAD_FORELDREPENGER_FØDSEL", "fp-12345", "FS38", FAGSAK, BRUKER_NAVN);
		doAssertJournalpost(journalposts.get(1), "UFO", "Søknad om Uføretrygd", "21998969", "PP01", FAGSAK, BRUKER_NAVN);
		doAssertJournalpost(journalposts.get(2), "FOR", "Bekreftelse fra Arbeidsgiver ifbm foreldrepenger", null, null, GENERELL_SAK, BRUKER_NAVN);
	}


	private void doAssertJournalpost(Journalpost jp, String tema, String tittel, String fagsakId, String fagsaksystem, Sakstype sakstype, String navn) {
		assertThat(jp.getTema()).isEqualTo(tema);
		assertThat(jp.getTittel()).isEqualTo(tittel);
		assertThat(jp.getSak().getFagsakId()).isEqualTo(fagsakId);
		assertThat(jp.getSak().getFagsaksystem()).isEqualTo(fagsaksystem);
		assertThat(jp.getSak().getSakstype()).isEqualTo(sakstype);
		assertThat(jp.getJournalstatus()).isEqualTo(JOURNALFOERT);
		if (jp.getJournalposttype() == I) {
			assertThat(jp.getAvsender().getId()).isEqualTo("12345678911");
			assertThat(jp.getAvsender().getType()).isEqualTo(FNR);
			assertThat(jp.getAvsender().getNavn()).isEqualTo(navn);
			assertThat(jp.getKanal()).isEqualTo(NAV_NO);
			assertThat(jp.getMottaker()).isNull();
		} else if (jp.getJournalposttype() == U) {
			assertThat(jp.getMottaker().getId()).isEqualTo("12345678911");
			assertThat(jp.getMottaker().getType()).isEqualTo(FNR);
			assertThat(jp.getMottaker().getNavn()).isEqualTo(navn);
			assertThat(jp.getKanal()).isEqualTo(NAV_NO);
			assertThat(jp.getAvsender()).isNull();
		}
	}


	@Test
	void shouldGetDokumentoversiktWhenOnlyForeldrepengerTemaQueried() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_for.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
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
		stubPensjonssaker("hentpensjonssaker_error.json");
		stubFagarkiv("finnjournalposter_missing_pen.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(data.getTema()).hasSize(1);
		Sakstema foreldrepenger = data.getTema().get(0);

		verify(1, getRequestedFor(urlMatching(".*/sammendrag")));
		assertThat(foreldrepenger.getJournalposter()).hasSize(2);
	}

	@Test
	void shouldFilterOutPensjonssakerWithArkivtemaNull() throws Exception {
		happyStubs();
		stubPensjonssaker("hentpensjonssaker_happy_arkivtema_null.json");

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_all.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();
		assertThat(data.getTema()).hasSize(2);

		Sakstema sakstemaPensjon = data.getTema().get(1);
		assertEquals("PEN", sakstemaPensjon.getKode());
		assertEquals("21998969", sakstemaPensjon.getJournalposter().get(0).getSak().getFagsakId());

		verify(1, getRequestedFor(urlMatching(".*/sammendrag")));
		assertThat(sakstemaPensjon.getJournalposter()).hasSize(1);
	}

	@Test
	void shouldNotCallFagarkivWhenTemaOnlyQuery() throws Exception {
		happyStubs();

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_tema_only.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();


		assertThat(data.getTema()).hasSize(2);
		verify(1, getRequestedFor(urlMatching(".*/springapi/sak/sammendrag")));
		verify(0, postRequestedFor(urlEqualTo("/fagarkiv")));
	}

	@Test
	void shouldGetOnlyTemaThatUserCanSee() throws Exception {
		happyStubs();
		stubSak("saker_ingen_innsyn.json");
		// testen over oppdateres hvis flere (eller færre) tema skal filtreres bort
		assertThat(Tema.unntattInnsynNavNo()).hasSize(5);

		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_tema_only.query");

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(1);
		// fra pensjonssaker
		assertThat(data.getTema().get(0).getKode()).isEqualTo("UFO");
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
	void shouldGetDokumentoversiktWhenTokenNotMatchingQueryIdentAndFullmaktExistsForTema() throws Exception {
		happyStubs();
		stubPdlFullmakt("pdl_fullmakt_for.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(1);
		// fullmakt FOR
		assertThat(data.getTema().get(0).getKode()).isEqualTo("FOR");
		assertThat(data.getFagsak().get(0).getTema()).isEqualTo("FOR");
		assertThat(data.getJournalposter().get(0).getTema()).isEqualTo("FOR");
	}

	@Test
	void shouldGetDokumentoversiktWhenTokenNotMatchingQueryIdentAndFullmaktExistsForTemaOnlyTemaInArguments() throws Exception {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_for_aap.json");
		stubSak("saker_happy_for_aap.json");
		stubPdlFullmakt("pdl_fullmakt_for_aap.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_for.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Dokumentoversikt data = graphQLResponse.getData().getDokumentoversiktSelvbetjening();

		assertThat(data.getTema()).hasSize(1);
		// fullmakt FOR,AAP men kun tema FOR i filter
		assertThat(data.getTema().get(0).getKode()).isEqualTo("FOR");
		assertThat(data.getTema().get(0).getJournalposter()).hasSize(1);
	}

	@Test
	void shouldGetEmptyDokumentoversiktWhenTokenNotMatchingQueryIdentAndFullmaktDoesNotCoverTemaArgumentInQuery() throws Exception {
		happyStubs();
		stubFagarkiv("finnjournalposter_happy_bar.json");
		stubPdlFullmakt("pdl_fullmakt_for_aap.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_bar.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
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
		stubPdlFullmakt("pdl_fullmakt_feil_bruker.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturns4xx() throws Exception {
		stubTokenx();
		stubPdlFullmakt(HttpStatus.FORBIDDEN);

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturns5xx() throws Exception {
		stubTokenx();
		stubPdlFullmakt(HttpStatus.INTERNAL_SERVER_ERROR);

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturnsInvalidJson() throws Exception {
		stubTokenx();
		stubPdlFullmakt("pdl_fullmakt_invalid_json.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndFullmaktReturnsInvalidJsonNoArray() throws Exception {
		stubTokenx();
		stubPdlFullmakt("pdl_fullmakt_invalid_json_no_array.json");

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
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
		stubPdlFullmakt();
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnUnauthorizedWhenTokenNotMatchingQueryIdentAndIngenFullmaktOmraader() throws Exception {
		stubTokenx();
		stubPdlFullmakt("pdl_fullmakt_ingen_omraader.json");
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/dokumentoversiktselvbetjening_all.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(FULLMEKTIG_ID), POST, new URI("/graphql"));
		ResponseEntity<GraphQLResponse> response = restTemplate.exchange(requestEntity, GraphQLResponse.class);

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(UNAUTHORIZED.getText());
	}

	@Test
	void shouldReturnBadRequestWhenQueryIdentInvalid() throws Exception {
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_invalid_ident.query");

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(BAD_REQUEST.getText());
	}

	@Test
	void shouldReturnBadRequestWhenQueryIdentBlank() throws Exception {
		ResponseEntity<GraphQLResponse> response = callDokumentoversikt("dokumentoversiktselvbetjening_blank_ident.query");

		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(BAD_REQUEST.getText());
	}

	private void happyStubs() {
		stubAzure();
		stubTokenx();
		stubPdlGenerell();
		stubSak();
		stubPensjonssaker();
		stubFagarkiv();
		stubPdlFullmakt();
	}

	private void happyStubs(String fileName) {
		stubAzure();
		stubTokenx();
		stubPdlGenerell();
		stubSak();
		stubPensjonssaker();
		stubFagarkiv(fileName);
		stubPdlFullmakt();
	}

	private void happyStubWithInnsyn(String fileName) {
		stubAzure();
		stubTokenx();
		stubPdlGenerell();
		stubSak();
		stubPensjonssaker();
		stubFagarkiv(fileName);
		stubPdlFullmakt();
	}

	private ResponseEntity<GraphQLResponse> callDokumentoversikt(final String queryfile) throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(BRUKER_ID), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentoversiktSubToken(final String queryfile) throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeadersSubToken(BRUKER_ID), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}
}
