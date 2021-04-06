package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;

public class HentTilgangDokumentException extends ResponseStatusException {
	@Getter
	private final String reasonCode;
	public HentTilgangDokumentException(String reasonCode, String message) {
		super(HttpStatus.FORBIDDEN, message);
		this.reasonCode = reasonCode;
	}

	@Override
	public HttpHeaders getResponseHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(NAV_REASON_CODE, reasonCode);
		return httpHeaders;
	}
}
