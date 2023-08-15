package no.nav.safselvbetjening.hentdokument;

class KafkaTechnicalException extends RuntimeException {
	public KafkaTechnicalException(String s, Throwable t) {
		super(s, t);
	}
}
