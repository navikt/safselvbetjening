package no.nav.safselvbetjening.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * GraphQL endepunktet til applikasjonen.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Controller
@Slf4j
@Protected
public class GraphQLController {
	private final GraphQLSchema graphQLSchema;
	private final TokenValidationContextHolder tokenValidationContextHolder;

	@Autowired
	public GraphQLController(GraphQLWiring graphQLWiring, TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		SchemaParser schemaParser = new SchemaParser();
		InputStreamReader schema = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("schemas/safselvbetjening.graphqls")));

		TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		this.graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, graphQLWiring.createRuntimeWiring());
	}

	@PostMapping(value = "/graphql", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> graphQLRequest(@RequestBody final GraphQLRequest request, final WebRequest webRequest) {
			ExecutionResult executionResult =
					GraphQL.newGraphQL(graphQLSchema).build()
							.execute(ExecutionInput.newExecutionInput()
									.query(request.getQuery())
									.operationName(request.getOperationName())
									.variables(request.getVariables() == null ? Collections.emptyMap() : request.getVariables())
									.context(createGraphQLContext(webRequest))
									.build());
			return executionResult.toSpecification();
	}

	GraphQLRequestContext createGraphQLContext(final WebRequest webRequest) {
		return GraphQLRequestContext.builder()
				.navCallId(isNotBlank(webRequest.getHeader(NAV_CALLID)) ? webRequest.getHeader(NAV_CALLID) : UUID.randomUUID().toString())
				.tokenValidationContext(tokenValidationContextHolder.getTokenValidationContext())
				.build();
	}
}
