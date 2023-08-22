package no.nav.safselvbetjening.dokumentoversikt.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DokumentoversiktExtensionTest {

	@Test
	void shouldGenerateDeviceCustomStringCef() {
		DokumentoversiktExtension extension = DokumentoversiktExtension.builder()
				.build();
		assertThat(extension.getDeviceCustomStringsCef())
				.isEqualTo("");
	}
}