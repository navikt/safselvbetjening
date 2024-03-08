package no.nav.safselvbetjening.endpoints.graphql.journalpost;

import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.endpoints.graphql.GraphQLResponse;
import no.nav.safselvbetjening.graphql.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Objects.requireNonNull;
import static no.nav.safselvbetjening.domain.Journalposttype.N;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Tema.HJE;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_KASSERT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKANNET_DOKUMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class JournalpostByIdTilgangIT extends AbstractJournalpostItest {

	/**
	 * Alt i orden
	 */
	@Test
	void skalQueryJournalpostById() {
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
	 * Tilgangregel: 1a
	 * Journalpost har bruker tilknytning i et eget bruker element (fnr) og i saksrelasjonen (aktørid)
	 * Hvis bruker ikke finnes i PDL så skal det returneres forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisBrukerJournalpostenGjelderIkkeKanUtledes() {
		stubDokarkivJournalpost();
		stubPdl("pdl-bruker-finnes-ikke.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1b
	 * Selvbetjening viser ikke journalposter før en hardkodet dato, konfigurert som safselvbetjening.tidligst-innsyn-dato
	 */
	@Test
	void skalGiForbiddenHvisJournalpostEldreEnnInnsynsdato() {
		stubDokarkivJournalpost("1b-journalpost-foer-tidligst-dato-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1b
	 * Selvbetjening viser ikke journalposter før en hardkodet dato, konfigurert som safselvbetjening.tidligst-innsyn-dato
	 * Hvis innsyn flagget er satt til en vises verdi og det skal vises så returneres journalposten
	 */
	@Test
	void skalHenteJournalpostHvisInnsynVisesSelvOmEldreEnnInnsynsdato() {
		stubDokarkivJournalpost("1b-journalpost-innsyn-vises-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();
		assertThat(journalpost.getJournalpostId()).isNotNull();
	}

	/**
	 * Tilgangsregel: 1c
	 * Midlertidige journalposter har status M eller MO. Det betyr at de nylig er mottatt av NAV (typisk fra skanning)
	 * Det kan være søknader bruker har under behandling og de har ingen sakstilknytning eller er ferdig journalført
	 * Hvis journalposten er en midlertidig journalpost og alle andre tilgangsregler er OK så skal dokumentet returneres
	 */
	@Test
	void skalHenteJournalpostHvisJournalpostErMidlertidig() {
		stubPdlGenerell();
		stubDokarkivJournalpost("1c-journalpost-midlertidig-ok.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertThat(journalpost.getJournalstatus()).isEqualTo(MOTTATT);
		assertThat(journalpost.getTema()).isEqualTo(HJE.name());
		assertThat(journalpost.getSak()).isNull();
	}

	/**
	 * Tilgangsregel: 1c
	 * Hvis journalposten er under arbeid så skal det returneres forbidden
	 */
	@Test
	void skalGiForbiddenHvisJournalpostErUnderArbeid() {
		stubPdlGenerell();
		stubDokarkivJournalpost("1c-journalpost-under-arbeid-forbidden.json");

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1d
	 * Hvis dokumentet har en feilregistrert saksrelasjon så skal det returneres en forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisSaksrelasjonFeilregistrert() {
		stubDokarkivJournalpost("1d-journalpost-feilregistrert-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1e
	 * Hvis journalposten har tema unntatt innsyn på journalpost og den er midlertidig så skal det returneres forbidden
	 *
	 * @see no.nav.safselvbetjening.domain.Tema
	 */
	@Test
	void skalGiForbiddenHvisJournalpostenHarTemaSomIkkeSkalGiInnsynPaaJournalpost() {
		stubDokarkivJournalpost("1e-journalpost-midlertidig-tema-far-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1e
	 * Hvis journalposten har tema unntatt innsyn på saksrelasjon og journalposten sitt tema er feil så skal det returneres forbidden
	 *
	 * @see no.nav.safselvbetjening.domain.Tema
	 */
	@Test
	void skalGiForbiddenHvisJournalpostenHarTemaSomIkkeSkalGiInnsynPaaSaksrelasjonOgJournalpostTemaErFeil() {
		stubDokarkivJournalpost("1e-journalpost-saksrelasjon-tema-far-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1f
	 * Fagpost kan skjerme dokumenter på forespørsel, det settes da et flagg på Journalpost
	 * Hvis journalpost er skjermet så skal det returneres forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisJournalpostErSkjermet() {
		stubDokarkivJournalpost("1f-journalpost-skjerming-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1g
	 * Bruker kan ikke se notater med mindre notatet er et FORVALTNINGSNOTAT (kategori på dokumentet)
	 */
	@Test
	void skalGiForbiddenHvisJournalpostIkkeErForvaltningsnotat() {
		stubDokarkivJournalpost("1g-journalpost-ikke-forvaltningsnotat-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 1g
	 * Bruker kan ikke se notater med mindre notatet er et FORVALTNINGSNOTAT (kategori på dokumentet)
	 */
	@Test
	void skalHenteJournalpostHvisJournalpostErForvaltningsnotat() {
		stubDokarkivJournalpost("1g-journalpost-forvaltningsnotat-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		Journalpost journalpost = graphQLResponse.getData().getJournalpostById();

		assertThat(journalpost.getJournalposttype()).isEqualTo(N);
	}

	/**
	 * Tilgangsregel: 1h
	 * Hvis innsyn flagget er satt til en skjules verdi så skal det returneres en forbidden feil
	 */
	@Test
	void skalGiForbidddenFeilHvisInnsynSkjules() {
		stubDokarkivJournalpost("1h-journalpost-innsyn-skjules-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(requireNonNull(response.getBody()).getErrors())
				.extracting(e -> e.getExtensions().getCode())
				.contains(ErrorCode.FORBIDDEN.getText());
	}

	/**
	 * Tilgangsregel: 2a
	 * Hvis journalpost ikke har innlogget bruker som avsender så skal brukerHarTilgang=false gis på dokumentene
	 */
	@Test
	void skalGiBrukerHarTilgangFalseHvisBrukerIkkeErAvsender() {
		stubDokarkivJournalpost("2a-journalpost-bruker-ikke-avsender-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.extracting("brukerHarTilgang").containsExactly(false, false);
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.flatExtracting("code").containsExactly(DENY_REASON_PARTSINNSYN, DENY_REASON_PARTSINNSYN);
	}

	/**
	 * Tilgangsregel: 2b
	 * Saksbehandlere sender dokumenter fra NAV til skanning.
	 * Disse har da journalposttype utgående, journalstatus lokalprint og en skannet mottakskanal
	 * Hvis dokumentet er skannet så skal brukerHarTilgang=false gis på dokumentene
	 */
	@Test
	void skalGiBrukerHarTilgangFalseHvisUtgaaendeJournalpostSkannet() {
		stubDokarkivJournalpost("2b-journalpost-lokal-skannet-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.extracting("brukerHarTilgang").containsExactly(false, false);
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.flatExtracting("code").containsExactly(DENY_REASON_SKANNET_DOKUMENT, DENY_REASON_SKANNET_DOKUMENT);
	}

	/**
	 * Tilgangsregel: 2e
	 * Fagpost kan skjerme dokumentvariant.
	 * Hvis dokumentvariant er skjermet så skal brukerHarTilgang=false gis på dokumentene
	 */
	@Test
	void skalGiBrukerHarTilgangFalseHvisDokumentVariantErSkjermet() {
		stubDokarkivJournalpost("2e-journalpost-dokument-dokumentvariant-skjermet-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.extracting("brukerHarTilgang").containsExactly(true, false);
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.flatExtracting("code").containsExactly("ok", DENY_REASON_GDPR);
	}

	/**
	 * Tilgangsregel: 2e
	 * Fagpost kan skjerme dokumenter.
	 * Hvis dokumenter er skjermet så skal brukerHarTilgang=false gis på dokumentene
	 */
	@Test
	void skalGiBrukerHarTilgangFalseHvisDokumentErSkjermet() {
		stubDokarkivJournalpost("2e-journalpost-dokument-skjermet-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.extracting("brukerHarTilgang").containsExactly(false, true);
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.flatExtracting("code").containsExactly(DENY_REASON_GDPR, "ok");
	}

	/**
	 * Tilgangsregel: 2f
	 * Fagpost kan logisk slette dokumenter.
	 * Hvis journalposten er kassert så skal brukerHarTilgang=false gis på dokumentene
	 */
	@Test
	void skalGiBrukerHarTilgangFalseHvisDokumentErKassert() {
		stubDokarkivJournalpost("2f-journalpost-dokument-kassert-ok.json");
		stubPdlGenerell();

		ResponseEntity<GraphQLResponse> response = queryJournalpostById();

		assertThat(response.getStatusCode()).isEqualTo(OK);
		GraphQLResponse graphQLResponse = response.getBody();
		assertThat(graphQLResponse).isNotNull();
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.extracting("brukerHarTilgang").containsExactly(true, false);
		assertThat(graphQLResponse.getData()
				.getJournalpostById().getDokumenter().stream()
				.flatMap(dokumentInfo -> dokumentInfo.getDokumentvarianter().stream()))
				.flatExtracting("code").containsExactly("ok", DENY_REASON_KASSERT);
	}

}
