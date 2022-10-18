package no.nav.safselvbetjening;

public final class TokenClaims {
	// Brukerident ligger i pid claim på tokenet for flyten idporten -> tokenx
	public static final String CLAIM_PID = "pid";
	// Brukerident ligger i sub claim på tokenet for flyten NAV loginservice -> tokenx
	public static final String CLAIM_SUB = "sub";

	private TokenClaims() {
		// noop
	}
}
