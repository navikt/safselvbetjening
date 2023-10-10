package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Tester fullmakt-relaterte tilgangsregler implementasjon definert i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
 */
public class HentDokumentTilgangFullmaktIT extends AbstractHentDokumentItest {

	@Test
	void shouldHentDokumentWhenTokenNotMatchingJournalpostOwnerIdentAndFullmaktExistsForTema() {
		stubAzure();
		stubTokenx();
		stubPdlFullmakt("pdl-fullmakt-tema-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	@Test
	void shouldHentDokumentWhenTokenNotMatchingPensjonJournalpostOwnerIdentAndFullmaktExistsForTemaMatchingPesysSak() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt("pdl-fullmakt-tema-ufo.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingPensjonJournalpostOwnerIdentAndFullmaktExistsForTemaNotMatchPesysSak() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt("pdl-fullmakt-tema-pen.json");
		stubDokarkivJournalpost("1c-hentdokument-pensjon-ok.json");
		stubPensjonHentBrukerForSak("pensjon-hentbrukerforsak-generell.json");
		// UFO pensjonssak
		stubPensjonssaker("pensjon-sak-sammendrag-generell.json");
		// PEN journalpost
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndFullmaktIkkeGittForTema() {
		stubAzure();
		stubTokenx();
		stubPdlFullmakt("pdl-fullmakt-tema-pen.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndNoFullmakt() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt();
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndWrongFullmakt() {
		stubTokenx();
		stubAzure();
		stubPdlGenerell();
		stubPdlFullmakt("pdl-fullmakt-feil-bruker.json");
		stubDokarkivJournalpost();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndFullmaktReturns4xx() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt(FORBIDDEN);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndFullmaktReturns5xx() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt(INTERNAL_SERVER_ERROR);
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndFullmaktReturnsInvalidJson() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt("pdl-fullmakt-invalid.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndFullmaktReturnsInvalidJsonNoArray() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt("pdl-fullmakt-invalid-no-array.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndIngenFullmaktTema() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt("pdl-fullmakt-ingen-tema.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}

	@Test
	void shouldReturnForbiddenWhenTokenNotMatchingJournalpostOwnerIdentAndSakTemaNoFullmakt() {
		stubTokenx();
		stubAzure();
		stubPdlFullmakt("pdl-fullmakt-tema-hje.json");
		stubDokarkivJournalpost("ukj-hentdokument-journalpost-sak-forskjellig-tema-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsFullmektig();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
	}
}
