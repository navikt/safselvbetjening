package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

/**
 * Et fødselsnummer som identifiserer en bruker. Inkluderer kontroller for å unngå at fødselsnummeret lekker ut i logger e.l.
 */
public final class Foedselsnummer extends Ident {
	private static final Consumer<String> VALIDATOR = identValidator(11, "Fødselsnummer");

	private Foedselsnummer(String value) {
		super(value, VALIDATOR);
	}

	/**
	 * @param value Et fødselsnummer
	 * @return Et Foedselsnummer med et gyldig fødselsnummer
	 * @throws IllegalArgumentException om value ikke er et fødselsnummer
	 */
	public static Foedselsnummer of(String value) {
		return new Foedselsnummer(trim(value));
	}

	public String toString() {
		return "Fødselsnummer(***********)";
	}
}
