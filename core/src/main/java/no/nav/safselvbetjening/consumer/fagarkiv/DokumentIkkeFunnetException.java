package no.nav.safselvbetjening.consumer.fagarkiv;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokumentIkkeFunnetException extends ConsumerFunctionalException {
	public DokumentIkkeFunnetException(String message, Throwable cause) {
		super(message, cause);
	}
}
