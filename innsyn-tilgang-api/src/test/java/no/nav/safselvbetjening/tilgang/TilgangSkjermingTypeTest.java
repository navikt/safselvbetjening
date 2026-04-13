package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.safselvbetjening.tilgang.TilgangSkjermingType.ARK;
import static no.nav.safselvbetjening.tilgang.TilgangSkjermingType.INGEN_SKJERMING;
import static no.nav.safselvbetjening.tilgang.TilgangSkjermingType.POL;
import static no.nav.safselvbetjening.tilgang.TilgangSkjermingType.UKJENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TilgangSkjermingTypeTest {

	static Stream<Arguments> skjermingTypeMappinger() {
		return Stream.of(
			arguments("ARK",       ARK),
			arguments("FEIL",      ARK),
			arguments("POL",       POL),
			arguments("FUTURE",    UKJENT),
			arguments("",          INGEN_SKJERMING),
			arguments(" ",         INGEN_SKJERMING),
			arguments(null,        INGEN_SKJERMING)
		);
	}

	@ParameterizedTest
	@MethodSource("skjermingTypeMappinger")
	void from_skalMappeKorrekt(String input, TilgangSkjermingType expected) {
		assertEquals(expected, TilgangSkjermingType.from(input));
	}
}
