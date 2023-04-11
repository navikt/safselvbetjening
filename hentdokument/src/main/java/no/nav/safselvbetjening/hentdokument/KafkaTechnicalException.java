package no.nav.safselvbetjening.hentdokument;

public class KafkaTechnicalException extends RuntimeException {

	public KafkaTechnicalException(String s, Throwable t) {
		super(s, t);
	}
}
