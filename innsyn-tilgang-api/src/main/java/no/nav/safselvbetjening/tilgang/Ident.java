package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

/**
 * En ident som identifiserer en bruker; Se de konkrete implementasjonene Foedselsnummer, Organisasjonsnummer og AktoerId
 *
 * @see Foedselsnummer
 * @see Organisasjonsnummer
 * @see AktoerId
 */
public sealed abstract class Ident extends JustA<String> permits Foedselsnummer, Organisasjonsnummer, AktoerId {
	protected Ident(String value, Consumer<String> validator) {
		super(value);
		validator.accept(value);
	}

	protected static Consumer<String> identNonNullValidator(String name) {
		return (value) -> {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException(name + " kan ikke være null eller blank");
			}
		};
	}

	protected static Consumer<String> identValidator(int requiredLength, String name) {
		return (value) -> {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException(name + " kan ikke være null eller blank");
			}
			if (requiredLength != -1 && value.trim().length() != requiredLength) {
				throw new IllegalArgumentException(name + " må ha akkurat " + requiredLength + " tegn");
			}
		};
	}

	protected static String trim(String value) {
		return value == null ? null : value.trim();
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + value + ")";
	}
}
