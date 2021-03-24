package no.nav.safselvbetjening.tilgang;

public class HentTilgangDokumentException extends RuntimeException {
	public HentTilgangDokumentException(String message) {
		super(message);
	}

	public HentTilgangDokumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
