package no.nav.safselvbetjening.tilgang;

import java.util.Set;

import lombok.Getter;

@Getter
public class UserNotMatchingTokenException extends RuntimeException {
	private final String ident;
	private final Set<String> identer;

	public UserNotMatchingTokenException(String ident, Set<String> identer) {
		this.ident = ident;
		this.identer = identer;
	}
}
