package no.nav.safselvbetjening.tilgang;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;

import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static org.springframework.http.HttpStatus.FORBIDDEN;

public class HentTilgangDokumentException extends ResponseStatusException {

	@Getter
	private final String reasonCode;

	public HentTilgangDokumentException(String reasonCode, String message) {
		super(FORBIDDEN, message);
		this.reasonCode = reasonCode;
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(NAV_REASON_CODE, reasonCode);
		return httpHeaders;
	}
}
