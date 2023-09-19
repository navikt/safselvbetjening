package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;

class KafkaTechnicalException extends ConsumerTechnicalException {
	public KafkaTechnicalException(String s, Throwable t) {
		super(s, t);
	}
}
