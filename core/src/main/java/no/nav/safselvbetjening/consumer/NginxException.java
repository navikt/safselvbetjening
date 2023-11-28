package no.nav.safselvbetjening.consumer;

public class NginxException extends ConsumerTechnicalException {
	public NginxException(String message, Throwable cause) {
		super(message, cause);
	}
}
