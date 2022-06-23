package no.nav.safselvbetjening.consumer.azure;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TokenConsumer {
	TokenResponse getClientCredentialToken(String scope);
}
