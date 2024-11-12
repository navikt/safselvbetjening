package no.nav.safselvbetjening.tilgang;

import java.util.function.Consumer;

/**
 * En aktør-id som identifiserer en bruker
 */
public final class AktoerId extends Ident {
	private static Consumer<String> VALIDATOR = identNonNullValidator("AktørId");

	private AktoerId(String value) {
		super(value, VALIDATOR);
	}

	/**
	 * Lag en ny aktørid.
	 *
	 * @param value En String som inneholder en aktørid
	 * @return En Aktørid
	 * @throws IllegalArgumentException om value er null eller tom
	 */
	public static AktoerId of(String value) {
		return new AktoerId(trim(value));
	}

	/**
	 * Lag en ny aktørid.
	 *
	 * @param value en String som inneholder en aktørid. Kan være null
	 * @return En Aktørid, eller null om value er null
	 * @throws IllegalArgumentException om value er en tom streng
	 */
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
