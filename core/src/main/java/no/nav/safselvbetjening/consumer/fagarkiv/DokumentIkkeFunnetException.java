package no.nav.safselvbetjening.consumer.fagarkiv;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class DokumentIkkeFunnetException extends ConsumerFunctionalException {
	public DokumentIkkeFunnetException(String message, Throwable cause) {
		super(message, cause);
	}
}
