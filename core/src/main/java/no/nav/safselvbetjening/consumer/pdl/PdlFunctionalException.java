package no.nav.safselvbetjening.consumer.pdl;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;

public class PdlFunctionalException extends ConsumerFunctionalException {
    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
