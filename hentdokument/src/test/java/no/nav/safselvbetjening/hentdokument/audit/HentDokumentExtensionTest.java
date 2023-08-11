package no.nav.safselvbetjening.hentdokument.audit;

import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HentDokumentExtensionTest {

	@Test
	void shouldGenerateDeviceCustomStringCef() {
		HentDokumentExtension extension = HentDokumentExtension.builder()
				.hentdokumentRequest(HentdokumentRequest.builder()
						.journalpostId("1000")
						.dokumentInfoId("1001")
						.variantFormat("ARKIV")
						.build())
				.build();
		assertThat(extension.getDeviceCustomStringsCef())
				.isEqualTo("cs1=1000 cs1Label=journalpostId cs2=1001 cs2Label=dokumentInfoId cs3=ARKIV cs3Label=variantFormat");
	}
}