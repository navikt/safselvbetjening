package no.nav.safselvbetjening.consumer;

public class ConsumerFunctionalException extends RuntimeException {
    public ConsumerFunctionalException(String message) {
        super(message);
    }

    public ConsumerFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
