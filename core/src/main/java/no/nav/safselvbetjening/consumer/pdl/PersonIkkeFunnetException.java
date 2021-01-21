package no.nav.safselvbetjening.consumer.pdl;


/**
 * Exception PersonIkkeFunnetException.
 *
 * @author Tak Wai Wang (Capgemini)
 */
public class PersonIkkeFunnetException extends RuntimeException {
    public PersonIkkeFunnetException(String message) {
        super(message);
    }

    public PersonIkkeFunnetException(Throwable cause, String message) {
        super(message, cause);
    }
}
