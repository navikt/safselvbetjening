package no.nav.safselvbetjening.consumer.dokarkiv;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class JournalpostIkkeFunnetException extends ConsumerFunctionalException {
	public JournalpostIkkeFunnetException(String message) {
		super(message);
	}
}
