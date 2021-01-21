package no.nav.safselvbetjening.consumer.pdl;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class PdlFunctionalException extends RuntimeException {
    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
