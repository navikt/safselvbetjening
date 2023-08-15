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
				.isEqualTo("flexString1=1000 flexString1Label=journalpostId flexString2=1001 flexString2Label=dokumentInfoId cs3=ARKIV cs3Label=variantFormat");
	}
}