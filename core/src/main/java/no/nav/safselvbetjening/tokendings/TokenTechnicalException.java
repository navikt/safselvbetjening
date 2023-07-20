package no.nav.safselvbetjening.tokendings;

import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;

public class TokenTechnicalException extends ConsumerTechnicalException {

	public TokenTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
