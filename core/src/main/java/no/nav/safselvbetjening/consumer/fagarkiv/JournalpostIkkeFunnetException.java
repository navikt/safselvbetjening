package no.nav.safselvbetjening.consumer.fagarkiv;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class JournalpostIkkeFunnetException extends ConsumerFunctionalException {
	public JournalpostIkkeFunnetException(String message, Throwable cause) {
		super(message, cause);
	}
}
