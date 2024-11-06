package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class OrganisasjonsnummerTest {

	@ParameterizedTest
	@ValueSource(strings = {"12345679", "", "1236540765", "  1234567  "})
	void shouldNotAcceptInputThatDoesNotMatchOrganisasjonsnummer(String input) {
		assertThrows(IllegalArgumentException.class, () -> Organisasjonsnummer.of(input));
	}

	@ParameterizedTest
	@ValueSource(strings = {"123456789", "123456789  ", " 123456789  "})
	void shouldAcceptInputThatDoesMatchOrganisasjonsnummer(String input) {
		assertDoesNotThrow(() -> Organisasjonsnummer.of(input));
	}

	@Test
	void identicalOrganisasjonsnummerShouldBeEqual() {
		assertEquals(Organisasjonsnummer.of("123456789"), Organisasjonsnummer.of("123456789"));
	}
}