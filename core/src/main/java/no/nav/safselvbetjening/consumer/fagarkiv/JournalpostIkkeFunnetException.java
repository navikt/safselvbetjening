package no.nav.safselvbetjening.consumer.fagarkiv;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class JournalpostIkkeFunnetException extends ConsumerFunctionalException {
	public JournalpostIkkeFunnetException(String message, Throwable cause) {
		super(message, cause);
	}
}
