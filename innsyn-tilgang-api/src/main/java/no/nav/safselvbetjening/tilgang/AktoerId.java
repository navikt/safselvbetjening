package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

public final class AktoerId extends Ident {
	private static Consumer<String> VALIDATOR = identNonNullValidator("AktørId");

	private AktoerId(String value) {
		super(value, VALIDATOR);
	}

	public static AktoerId of(String value) {
		return new AktoerId(value.trim());
	}

	public static AktoerId ofNullable(String value) {
		if (value == null) {
			return null;
		}
		return of(value);
	}

	public String toString() {
		return "AktoerId(" + value + ")";
	}
}
