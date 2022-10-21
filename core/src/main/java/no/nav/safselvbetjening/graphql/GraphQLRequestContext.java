package no.nav.safselvbetjening.graphql;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.nav.security.token.support.core.context.TokenValidationContext;

@Value
@Builder
public class GraphQLRequestContext {
	public static final String KEY = GraphQLRequestContext.class.getName();

	String navCallId;
	@ToString.Exclude
	TokenValidationContext tokenValidationContext;
}
