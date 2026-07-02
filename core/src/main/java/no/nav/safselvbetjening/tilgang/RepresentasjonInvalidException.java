package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import no.nav.safselvbetjening.representasjon.Representasjonsforhold;

@Getter
public class RepresentasjonInvalidException extends RuntimeException {
	private final Representasjonsforhold representasjonsforhold;
	private final String gjeldendeTema;

	public RepresentasjonInvalidException(Representasjonsforhold representasjonsforhold, String gjeldendeTema) {
		this.representasjonsforhold = representasjonsforhold;
		this.gjeldendeTema = gjeldendeTema;
	}

}
