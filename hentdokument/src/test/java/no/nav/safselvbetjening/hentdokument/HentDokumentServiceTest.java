package no.nav.safselvbetjening.hentdokument;

import org.junit.jupiter.api.Test;

import static no.nav.safselvbetjening.hentdokument.HentDokumentService.HENTDOKUMENT_TILGANG_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;

class HentDokumentServiceTest {

	/**
	 * Avstemming av flere/færre felt som hentes fra dokarkiv safintern journalpost tjenesten
	 * Risiko er at man fjerner felt som ikke skal fjernes
	 */
	@Test
	void skalHenteDokarkivFields() {
		assertThat(HENTDOKUMENT_TILGANG_FIELDS)
				.containsExactlyInAnyOrder(
						"journalpostId", "fagomraade", "status", "type", "skjerming", "mottakskanal", "utsendingskanal", "innsyn",
						"bruker", "avsenderMottaker", "relevanteDatoer", "saksrelasjon",
						"dokumenter.dokumentInfoId", "dokumenter.tilknyttetSom", "dokumenter.kassert", "dokumenter.kategori", "dokumenter.skjerming", "dokumenter.fildetaljer"
				);
	}
}