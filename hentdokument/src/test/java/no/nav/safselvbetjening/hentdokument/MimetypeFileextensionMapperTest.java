package no.nav.safselvbetjening.hentdokument;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_PDF;

class MimetypeFileextensionMapperTest {
	@Test
	void shouldGetFileExtensionFromMediaType() {
		String fileextension = MimetypeFileextensionMapper.toFileextension(APPLICATION_PDF);
		assertThat(fileextension, is(".pdf"));
	}

	@Test
	void shouldGetFileExtensionFromString() {
		String fileextension = MimetypeFileextensionMapper.toFileextension("application/pdf");
		assertThat(fileextension, is(".pdf"));
	}
}