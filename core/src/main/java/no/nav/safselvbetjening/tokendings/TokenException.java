package no.nav.safselvbetjening.tokendings;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class TokenException extends ConsumerFunctionalException {

	public TokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
