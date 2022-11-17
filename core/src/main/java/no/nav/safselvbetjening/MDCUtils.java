package no.nav.safselvbetjening;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class MDCUtils {
	public static final String MDC_CALL_ID = "callId";
	public static final String MDC_CONSUMER_ID = "consumerId";
	private static final String UKJENT_CONSUMERID = "ukjent";

	public static String getCallId() {
		final String callId = MDC.get(MDC_CALL_ID);
		return isBlank(callId) ? UUID.randomUUID().toString() : callId;
	}

	public static String getConsumerIdFromToken(TokenValidationContext tokenValidationContext) {
		Optional<JwtToken> firstValidToken = tokenValidationContext.getFirstValidToken();
		if (firstValidToken.isPresent()) {
			JwtToken jwtToken = firstValidToken.get();
			return getClientId(jwtToken);
		}
		return UKJENT_CONSUMERID;
	}

	private static String getClientId(final JwtToken jwtToken) {
		String claim = jwtToken.getJwtTokenClaims().getStringClaim("client_id");
		if (claim == null) {
			return UKJENT_CONSUMERID;
		} else {
			return claim;
		}
	}

	private MDCUtils() {
		// ingen instansiering
	}
}
