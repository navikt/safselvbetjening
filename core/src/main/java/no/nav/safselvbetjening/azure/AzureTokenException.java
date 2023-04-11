package no.nav.safselvbetjening.azure;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class AzureTokenException extends ConsumerFunctionalException {

	public AzureTokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
