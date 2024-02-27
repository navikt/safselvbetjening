package no.nav.safselvbetjening.endpoints.graphql.journalpost;

import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.endpoints.AbstractItest;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import no.nav.safselvbetjening.graphql.GraphQLRequest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.Datotype.DATO_DOKUMENT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_OPPRETTET;
import static no.nav.safselvbetjening.domain.Datotype.DATO_REGISTRERT;
import static no.nav.safselvbetjening.domain.DomainConstants.DOKUMENT_TILGANG_STATUS_OK;
import static no.nav.safselvbetjening.domain.Journalposttype.I;
import static no.nav.safselvbetjening.domain.Journalstatus.JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.Variantformat.ARKIV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

abstract class AbstractJournalpostItest extends AbstractItest {

	protected static final String BRUKER_ID = "12345678911";
	protected static final String JOURNALPOST_ID = "400000000";

	@BeforeEach
	void setUp() {
		stubAzure();
	}

	protected static void assertInngaaendeJournalpost(Journalpost journalpost) {
		assertThat(journalpost.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(journalpost.getJournalposttype()).isEqualTo(I);
		assertThat(journalpost.getTema()).isEqualTo("HJE");
		assertThat(journalpost.getJournalstatus()).isEqualTo(JOURNALFOERT);
		assertThat(journalpost.getKanal()).isEqualTo(NAV_NO);
		assertThat(journalpost.getTittel()).isEqualTo("Søknad om hjelpemidler");
		assertThat(journalpost.getEksternReferanseId()).isEqualTo("11111111-2222-3333-4444-555555555555");
		assertThat(journalpost.getAvsender().getId()).isEqualTo("12345678911");
		assertThat(journalpost.getAvsender().getType()).isEqualTo(FNR);
		assertThat(journalpost.getSak().getFagsakId()).isEqualTo("9000");
		assertThat(journalpost.getSak().getFagsaksystem()).isEqualTo("HJELPEMIDLER");
		assertThat(journalpost.getSak().getSakstype()).isEqualTo(FAGSAK);
		assertThat(journalpost.getRelevanteDatoer())
				.hasSize(4)
				.contains(new RelevantDato(LocalDateTime.parse("2023-09-12T15:42:13"), DATO_JOURNALFOERT),
						new RelevantDato(LocalDateTime.parse("2023-08-16T13:15:00"), DATO_OPPRETTET),
						new RelevantDato(LocalDateTime.parse("2023-08-16T13:15:00"), DATO_REGISTRERT),
						new RelevantDato(LocalDateTime.parse("2023-08-16T13:15:00"), DATO_DOKUMENT));
	}

	protected static void assertDokumenter(List<DokumentInfo> dokumenter) {
		assertThat(dokumenter).hasSize(2);
		assertHoveddokument(dokumenter.get(0));
		assertVedlegg(dokumenter.get(1));
	}

	private static void assertHoveddokument(DokumentInfo hoveddokument) {
		assertThat(hoveddokument.getDokumentInfoId()).isEqualTo("410000000");
		assertThat(hoveddokument.getBrevkode()).isEqualTo("NAV 10-07.53");
		assertThat(hoveddokument.getTittel()).isEqualTo("Søknad om hjelpemidler");
		assertThat(hoveddokument.getSensitivtPselv()).isNull();

		assertHoveddokumentVarianter(hoveddokument.getDokumentvarianter());
	}

	private static void assertHoveddokumentVarianter(List<Dokumentvariant> dokumentvarianter) {
		assertThat(dokumentvarianter)
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::getFiltype,
						Dokumentvariant::getFilstorrelse,
						Dokumentvariant::getCode,
						Dokumentvariant::isBrukerHarTilgang)
				.hasSize(1)
				.containsExactly(tuple(ARKIV,
						"11111111-2222-3333-4444-000000000001",
						"PDF",
						1024,
						List.of(DOKUMENT_TILGANG_STATUS_OK),
						true));
	}

	private static void assertVedlegg(DokumentInfo vedlegg) {
		assertThat(vedlegg.getDokumentInfoId()).isEqualTo("420000000");
		assertThat(vedlegg.getBrevkode()).isEqualTo("L7");
		assertThat(vedlegg.getTittel()).isEqualTo("Kvitteringsside for dokumentinnsending");
		assertThat(vedlegg.getSensitivtPselv()).isNull();

		assertVedleggVarianter(vedlegg.getDokumentvarianter());
	}

	private static void assertVedleggVarianter(List<Dokumentvariant> dokumentvarianter) {
		assertThat(dokumentvarianter)
				.extracting(Dokumentvariant::getVariantformat,
						Dokumentvariant::getFiluuid,
						Dokumentvariant::getFiltype,
						Dokumentvariant::getFilstorrelse,
						Dokumentvariant::getCode,
						Dokumentvariant::isBrukerHarTilgang)
				.hasSize(1)
				.containsExactly(tuple(ARKIV,
						"11111111-2222-3333-4444-000000000003",
						"PDF",
						4096,
						List.of(DOKUMENT_TILGANG_STATUS_OK),
						true));
	}

	protected ResponseEntity<GraphQLResponse> queryJournalpostById() {
		return queryJournalpostById("journalpost_by_id_all.query", BRUKER_ID);
	}

	protected ResponseEntity<GraphQLResponse> queryJournalpostByIdAsFullmektig() {
		return queryJournalpostById("journalpost_by_id_all.query", FULLMEKTIG_ID);
	}

	protected ResponseEntity<GraphQLResponse> queryJournalpostById(String innloggetBrukerId) {
		return queryJournalpostById("journalpost_by_id_all.query", innloggetBrukerId);
	}

	protected ResponseEntity<GraphQLResponse> queryJournalpostById(String queryfile, String innloggetBrukerId) {
		return queryJournalpostById(queryfile, innloggetBrukerId, JOURNALPOST_ID);
	}

	protected ResponseEntity<GraphQLResponse> queryJournalpostById(String queryfile, String innloggetBrukerId, String journalpostId) {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("queries/" + queryfile), null, Map.of("journalpostId", journalpostId));
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, httpHeaders(innloggetBrukerId), POST, URI.create("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	protected static void stubDokarkivJournalpost() {
		stubDokarkivJournalpost("1c-journalpost-ok.json");
	}

	protected static void stubDokarkivJournalpost(HttpStatus httpStatus) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)
				.willReturn(aResponse()
						.withStatus(httpStatus.value())));
	}

	protected static void stubDokarkivJournalpost(String fil) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokarkiv/journalpostbyid/" + fil)));
	}
}
