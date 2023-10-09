package no.nav.safselvbetjening.endpoints.hentDokument;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKJULT_INNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_SKANNET;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_SKJULT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Tester tilgangsregler implementasjon definert i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
 */
public class HentDokumentTilgangIT extends AbstractHentDokumentItest {

	@Test
	void hentDokumentPdlNotFound() {
		stubAzure();
		stubDokarkivJournalpost();
		stubPdl("pdl-bruker-finnes-ikke.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_PARTSINNSYN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
	}

	@Test
	void hentMidlertidigDokumentHappyPath() {
		stubAzure();
		stubDokarkivJournalpost("1c-hentdokument-midlertidig-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentTilgangAvvist() {
		stubAzure();
		stubDokarkivJournalpost("1f-hentdokument-journalpost-skjerming-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_GDPR));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_GDPR);
	}

	@Test
	void hentDokumentWhenInnsynIsVises() {
		stubAzure();
		stubDokarkivJournalpost("1b-hentdokument-innsyn-vises-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void shouldHentDokumentTilgangAvvistInnsynSkjules() {
		stubAzure();
		stubDokarkivJournalpost("1h-hentdokument-skjules-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_SKJULT_INNSYN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_SKJULT);
	}

	@Test
	void shouldReturnForbiddenWhenLokalprintSkannet() {
		stubAzure();
		stubDokarkivJournalpost("2b-hentdokument-lokal-skannet-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_SKANNET_DOKUMENT));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_SKANNET);
	}
}
