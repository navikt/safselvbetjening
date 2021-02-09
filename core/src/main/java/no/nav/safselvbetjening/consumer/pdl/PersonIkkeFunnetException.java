package no.nav.safselvbetjening.consumer.pdl;


import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;

/**
 * Exception PersonIkkeFunnetException.
 *
 * @author Tak Wai Wang (Capgemini)
 */
public class PersonIkkeFunnetException extends ConsumerTechnicalException {
    public PersonIkkeFunnetException(String message) {
        super(message);
    }

    public PersonIkkeFunnetException(Throwable cause, String message) {
        super(message, cause);
    }
}
