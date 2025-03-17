package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import no.nav.safselvbetjening.fullmektig.Fullmakt;

@Getter
public class FullmaktInvalidException extends RuntimeException {
	private final Fullmakt fullmakt;
	private final String gjeldendeTema;

	public FullmaktInvalidException(Fullmakt fullmakt, String gjeldendeTema) {
		this.fullmakt = fullmakt;
		this.gjeldendeTema = gjeldendeTema;
	}

}
