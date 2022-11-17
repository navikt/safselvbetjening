package no.nav.safselvbetjening.consumer;

public class ConsumerTechnicalException extends RuntimeException {
    public ConsumerTechnicalException(String message) {
        super(message);
    }

    public ConsumerTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
