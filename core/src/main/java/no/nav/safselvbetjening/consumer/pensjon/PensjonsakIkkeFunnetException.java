package no.nav.safselvbetjening.consumer.pensjon;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class PensjonsakIkkeFunnetException extends ConsumerFunctionalException {
	public PensjonsakIkkeFunnetException(String message) {
		super(message);
	}
}
