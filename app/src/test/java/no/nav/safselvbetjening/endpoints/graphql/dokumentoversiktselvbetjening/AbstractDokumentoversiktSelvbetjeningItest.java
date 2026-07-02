package no.nav.safselvbetjening.endpoints.graphql.dokumentoversiktselvbetjening;

import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.HttpMethod.POST;

public class AbstractDokumentoversiktSelvbetjeningItest extends AbstractItest {
	protected static final String BRUKER_ID = "12345678911";
	protected static final String BRUKER_NAVN = "HARRY POTTER";
	protected static final String UKJENT_MOTTAKER = "Ukjent mottaker";
	protected static final String UKJENT_AVSENDER = "Ukjent avsender";

	protected void happyStubs() {
		stubTokenx();
		stubNaisTexasToken();
		stubPdlGenerell();
		stubSak();
		stubPensjonssaker();
		stubFagarkiv();
		stubReprApiRepresentasjon();
	}

	protected void happyStubs(String fagarkivFilename) {
		stubTokenx();
		stubNaisTexasToken();
		stubPdlGenerell();
		stubSak();
		stubPensjonssaker();
		stubFagarkiv(fagarkivFilename);
		stubReprApiRepresentasjon();
	}

	protected void happyStubs(String fagarkivFilename, String sakFilename) {
		stubTokenx();
		stubNaisTexasToken();
		stubPdlGenerell();
		stubSak(sakFilename);
		stubPensjonssaker();
		stubFagarkiv(fagarkivFilename);
		stubReprApiRepresentasjon();
	}

	protected void happyStubWithInnsyn(String fileName) {
		stubTokenx();
		stubNaisTexasToken();
		stubPdlGenerell();
		stubSak();
		stubPensjonssaker();
		stubFagarkiv(fileName);
		stubReprApiRepresentasjon();
	}

	protected ResponseEntity<GraphQLResponse> callDokumentoversikt(final String queryfile) throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(BRUKER_ID), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	protected ResponseEntity<GraphQLResponse> callDokumentoversiktSubToken(final String queryfile) throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeadersSubToken(BRUKER_ID), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}
}
