package no.nav.safselvbetjening.azure;

import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;

public class AzureTokenTechnicalException extends ConsumerTechnicalException {
	public AzureTokenTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
