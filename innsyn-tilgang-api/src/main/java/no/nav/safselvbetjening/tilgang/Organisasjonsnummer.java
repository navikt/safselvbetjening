package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

public final class Organisasjonsnummer extends Ident {
	private static final Consumer<String> VALIDATOR = identValidator(9, "Organisasjonsnummer");

	private Organisasjonsnummer(String value) {
		super(value, VALIDATOR);
	}

	public static Organisasjonsnummer of(String value) {
		return new Organisasjonsnummer(value.trim());
	}
}
