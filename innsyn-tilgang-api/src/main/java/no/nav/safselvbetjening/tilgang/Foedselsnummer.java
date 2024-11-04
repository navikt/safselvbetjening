package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

public final class Foedselsnummer extends Ident {
	private static final Consumer<String> VALIDATOR = identValidator(11, "Fødselsnummer");

	private Foedselsnummer(String value) {
		super(value, VALIDATOR);
	}

	public static Foedselsnummer of(String value) {
		return new Foedselsnummer(value.trim());
	}

	public String toString() {
		return "Fødselsnummer(***********)";
	}
}
