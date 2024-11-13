package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


class IdentTest {

	@ParameterizedTest
	@ValueSource(strings = {"12345678901", " 12345678901 ", "123456789", " - ", "AB:1234", "123654098765", "  1234567  ", "123456789", "123456789  ", " 123456789  "})
	void shouldAcceptInputThatExistsToday(String input) {
		assertDoesNotThrow(() -> Ident.of(input));
	}

	@ParameterizedTest
	@CsvSource(value = {"12345678901,***********", " 12345678901 ,***********", " - ,*", "AB:1234,*******"}, ignoreLeadingAndTrailingWhitespace = false)
	void shouldCensorValueInToStringCorrectly(String input, String expectedCensor) {
		assertEquals("Ident(" + expectedCensor + ")", Ident.of(input).toString());
	}

	@Test
	void identicalOrganisasjonsnummerShouldBeEqual() {
		assertEquals(Ident.of("123456789"), Ident.of("123456789"));
	}

	@Test
	void identicalOrganisasjonsnummerShouldBeEqualEvenIfPadded() {
		assertEquals(Ident.of("123456789"), Ident.of("123456789  "));
	}

	@Test
	void identicalFoedselsnummerShouldBeEqual() {
		assertEquals(Ident.of("12345678901"), Ident.of("12345678901"));
	}
}