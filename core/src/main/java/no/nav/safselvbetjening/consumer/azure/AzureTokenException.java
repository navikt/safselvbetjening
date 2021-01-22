package no.nav.safselvbetjening.consumer.azure;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class AzureTokenException extends RuntimeException {
    public AzureTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
