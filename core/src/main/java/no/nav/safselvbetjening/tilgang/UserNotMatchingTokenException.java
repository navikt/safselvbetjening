package no.nav.safselvbetjening.tilgang;

import lombok.Getter;

import java.util.Set;

@Getter
public class UserNotMatchingTokenException extends RuntimeException {
	private final String ident;
	private final Set<String> identer;

	public UserNotMatchingTokenException(String ident, Set<String> identer) {
		this.ident = ident;
		this.identer = identer;
	}
}
