package no.nav.safselvbetjening.tilgang;

import java.util.List;

import lombok.Getter;

@Getter
public class UserNotMatchingTokenException extends RuntimeException {
	private final String ident;
	private final List<String> identer;

	public UserNotMatchingTokenException(String ident, List<String> identer) {
		this.ident = ident;
		this.identer = identer;
	}
}
