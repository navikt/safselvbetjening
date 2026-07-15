package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.hentdokument.HentDokumentService.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;

/// Generelle representasjon tester
public class HentDokumentTilgangRepresentasjonIT extends AbstractHentDokumentItest {

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		stubNaisTexasExchangeToken();
	}

	/// Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	/// Hvis repr-api returnerer vergemål og fullmakt for A der B er både vergehaver og fullmektig.
	/// Der kun vergemål tema matcher tema dokumentet gjelder så skal dokument hentes
	///
	/// Hvis dokumentet er et inngående hoveddokument med kanal NAV_NO skal det ikke genereres HoveddokumentLest hendelse
	@Test
	void skalHenteDokumentHvisPaaloggetBrukerErVergeOgFullmektigDerKunVergemaalHarGyldigTema() {
		stubReprApiRepresentasjon("repr-api-representasjon-vergemaal-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/// Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har vergemål overfor bruker B
	/// Hvis repr-api returnerer vergemål og fullmakt for A der B er både vergehaver og fullmektig.
	/// Der kun fullmakt tema matcher tema dokumentet gjelder så skal dokument hentes
	///
	/// Hvis dokumentet er et inngående hoveddokument med kanal NAV_NO skal det ikke genereres HoveddokumentLest hendelse
	@Test
	void skalHenteDokumentHvisPaaloggetBrukerErVergeOgFullmektigDerKunFullmaktHarGyldigTema() {
		stubReprApiRepresentasjon("repr-api-representasjon-fullmakt-hje.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertOkArkivResponse(responseEntity);
		HoveddokumentLest hoveddokumentLest = readFromHoveddokumentLestTopic();
		assertThat(hoveddokumentLest).isNull();
	}

	/// Hvis pålogget bruker er 22222222222 (A) og dokumentet tilhører 12345678911 (B) så skal man undersøke om bruker A har fullmakt overfor bruker B
	/// Hvis repr-api returnerer fullmakt med representant 33333333333 (C) så skal det returneres en Forbidden feil
	@Test
	void skalGiForbiddenFeilHvisRepresentantIkkeErInnloggetBruker() {
		stubReprApiRepresentasjon("repr-api-representasjon-feil-representant.json");
		stubDokarkivJournalpost();
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokumentAsRepresentant();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
	}
}
