package no.nav.safselvbetjening.tilgang;


/**
 * En ident som identifiserer en bruker; enten et fødselsnummer, et organisasjonsnummer eller en aktørid
 * Inkluderer kontroller for å unngå at eventuelle fødselsnummer lekker ut i logger e.l.
 */
public class Ident extends JustA<String> {
	protected Ident(String value) {
		super(value);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Ident kan ikke være null eller blank");
		}
	}

	/**
	 * @param value En String med et fødselsnummer, et organisasjonsnummer eller en aktørid
	 * @return En Ident med et fødselsnummer, et organisasjonsnummer eller en aktørid
	 * @throws IllegalArgumentException om value er null eller blank
	 */
	public static Ident of(String value) {
		return new Ident(trim(value));
	}

	/**
	 * Lag en ny Ident.
	 *
	 * @param value En String med et fødselsnummer, et organisasjonsnummer eller en aktørid. Kan være null
	 * @return En Ident, eller null om value er null eller tom
	 */
	public static Ident ofNullable(String value) {
		if (value == null || trim(value).isEmpty()) {
			return null;
		}
		return of(value);
	}

	protected static String trim(String value) {
		return value == null ? null : value.trim();
	}

	public String toString() {
		return "Ident(" + "*".repeat(value.length()) + ")";
	}
}
