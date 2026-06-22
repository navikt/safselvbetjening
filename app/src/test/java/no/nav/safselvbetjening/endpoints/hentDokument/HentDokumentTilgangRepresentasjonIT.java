package no.nav.safselvbetjening.endpoints.hentDokument;

import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/// Generelle representasjon tester
public class HentDokumentTilgangRepresentasjonIT extends AbstractHentDokumentItest {

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		stubTokenx();
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

}
