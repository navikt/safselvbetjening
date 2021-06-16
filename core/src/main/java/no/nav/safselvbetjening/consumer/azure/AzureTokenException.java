package no.nav.safselvbetjening.consumer.azure;

import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class AzureTokenException extends ConsumerTechnicalException {
	public AzureTokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
