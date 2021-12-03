package no.nav.safselvbetjening.graphql;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.nav.security.token.support.core.context.TokenValidationContext;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class GraphQLRequestContext {
	public static final String KEY = GraphQLRequestContext.class.getName();

	private final String navCallId;
	@ToString.Exclude
	private final TokenValidationContext tokenValidationContext;
}
