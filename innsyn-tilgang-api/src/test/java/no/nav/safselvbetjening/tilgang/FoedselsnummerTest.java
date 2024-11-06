package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FoedselsnummerTest {

	@ParameterizedTest
	@ValueSource(strings = {"123456789", "", "123654098765", "  1234567  "})
	void shouldNotAcceptInputThatDoesNotMatchFoedselsnummer(String input) {
		assertThrows(IllegalArgumentException.class, () -> Foedselsnummer.of(input));
	}

	@ParameterizedTest
	@ValueSource(strings = {"12345678901", " 12345678901 "})
	void shouldAcceptInputThatDoesMatchFoedselsnummer(String input) {
		assertDoesNotThrow(() -> Foedselsnummer.of(input));
	}

	@Test
	void identicalFoedselsnummerShouldBeEqual() {
		assertEquals(Foedselsnummer.of("12345678901"), Foedselsnummer.of("12345678901"));
	}
}