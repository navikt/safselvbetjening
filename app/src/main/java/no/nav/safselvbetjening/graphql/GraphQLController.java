package no.nav.safselvbetjening.graphql;

import com.github.benmanes.caffeine.cache.Cache;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static no.nav.safselvbetjening.cache.CacheConfig.GRAPHQL_QUERY_CACHE;
import static no.nav.safselvbetjening.graphql.GraphQLRequestContext.KEY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * GraphQL endepunktet til applikasjonen.
 */
@Controller
@Slf4j
@Protected
public class GraphQLController {

	private final GraphQLSchema graphQLSchema;
	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final Cache<String, PreparsedDocumentEntry> cache;

	@SuppressWarnings("unchecked")
	public GraphQLController(GraphQLWiring graphQLWiring,
							 TokenValidationContextHolder tokenValidationContextHolder,
							 CacheManager cacheManager,
							 CacheMetricsRegistrar cacheMetricsRegistrar) throws IOException {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		SchemaParser schemaParser = new SchemaParser();
		TypeDefinitionRegistry typeRegistry;
		try (InputStreamReader schema = new InputStreamReader(requireNonNull(getClass().getClassLoader().getResourceAsStream("schemas/safselvbetjening.graphqls")))) {
			typeRegistry = schemaParser.parse(schema);
		}
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		this.graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, graphQLWiring.createRuntimeWiring());
		org.springframework.cache.Cache cache = cacheManager.getCache(GRAPHQL_QUERY_CACHE);
		cacheMetricsRegistrar.bindCacheToRegistry(cache);
		this.cache = (Cache<String, PreparsedDocumentEntry>) requireNonNull(cache).getNativeCache();
	}

	@PostMapping(value = "/graphql", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> graphQLRequest(@RequestBody final GraphQLRequest request, final WebRequest webRequest) {
		ExecutionResult executionResult =
				GraphQL.newGraphQL(graphQLSchema)
						// https://www.graphql-java.com/documentation/execution/#query-caching
						.preparsedDocumentProvider((executionInput, parseAndValidateFunction) -> {
							Function<String, PreparsedDocumentEntry> mapCompute = key -> parseAndValidateFunction.apply(executionInput);
							return CompletableFuture.completedFuture(
									cache.get(executionInput.getQuery(), mapCompute));
						})
						.build()
						.execute(ExecutionInput.newExecutionInput()
								.query(request.query())
								.operationName(request.operationName())
								.variables(request.variables() == null ? Collections.emptyMap() : request.variables())
								.graphQLContext((c) -> c.put(KEY, createGraphQLContext(webRequest)))
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
