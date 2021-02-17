package no.nav.safselvbetjening.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * GraphQL endepunktet til applikasjonen.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Controller
@Slf4j
public class GraphQLController {
    private final GraphQLSchema graphQLSchema;

    @Autowired
    public GraphQLController(GraphQLWiring graphQLWiring) {
        SchemaParser schemaParser = new SchemaParser();
        InputStreamReader schema = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("schemas/safselvbetjening.graphqls")));

        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        this.graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, graphQLWiring.createRuntimeWiring());
    }

    @PostMapping(value = "/graphql", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> graphQLRequest(@RequestBody GraphQLRequest request) {
        ExecutionResult executionResult =
                GraphQL.newGraphQL(graphQLSchema).build()
                        .execute(ExecutionInput.newExecutionInput()
                        .query(request.getQuery())
                        .operationName(request.getOperationName())
                        .variables(request.getVariables() == null ? Collections.emptyMap() : request.getVariables())
                        .build());
        return executionResult.toSpecification();
    }
}
