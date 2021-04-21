package no.nav.safselvbetjening.hentdokument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class HentDokumentValidatorTest {
	private final HentDokumentValidator validator = new HentDokumentValidator();

	@ParameterizedTest
	@ValueSource(strings = {"Robert'); DROP TABLE Students; --", "12345abcd"})
	void shouldThrowExceptionWhenJournalpostIdNotNumeric(String journalpostId) {
		assertThrows(HentdokumentRequestException.class, () ->
				validator.validate(HentdokumentRequest.builder().journalpostId(journalpostId).build()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"Gandalf", "12345a"})
	void shouldThrowExceptionWhenDokumentInfoIdNotNumeric(String dokumentInfoId) {
		assertThrows(HentdokumentRequestException.class, () ->
				validator.validate(HentdokumentRequest.builder().dokumentInfoId(dokumentInfoId).build()));
	}

	@Test
	void shouldThrowExceptionWhenVariantFormatNotInAllowedSet() {
		assertThrows(HentdokumentRequestException.class, () ->
				validator.validate(HentdokumentRequest.builder().variantFormat("DOK").build()));
	}
}