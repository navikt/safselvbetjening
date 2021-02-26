package no.nav.safselvbetjening.consumer;


public class PersonIkkeFunnetException extends ConsumerFunctionalException {
    public PersonIkkeFunnetException(String message) {
        super(message);
    }

    public PersonIkkeFunnetException(String message, Throwable cause) {
        super(message, cause);
    }
}
