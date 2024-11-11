package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

/**
 * Et organisasjonsnummer som identifiserer en bruker
 */
public final class Organisasjonsnummer extends Ident {
	private static final Consumer<String> VALIDATOR = identValidator(9, "Organisasjonsnummer");

	private Organisasjonsnummer(String value) {
		super(value, VALIDATOR);
	}

	/**
	 * @param value et organisasjonsnummer. Kan ikke være null
	 * @return Et gyldig organisasjonsnummer
	 * @throws IllegalArgumentException om value ikke er et gyldig organisasjonsummer
	 */
	public static Organisasjonsnummer of(String value) {
		return new Organisasjonsnummer(trim(value));
	}
}
