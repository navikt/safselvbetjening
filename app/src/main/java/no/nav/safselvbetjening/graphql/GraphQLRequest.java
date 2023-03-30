package no.nav.safselvbetjening.graphql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record GraphQLRequest(
		String query,
		String operationName,
		Map<String, Object> variables
) {

	@JsonCreator
	public GraphQLRequest(@JsonProperty("query") String query,
						  @JsonProperty("operationName") String operationName,
						  @JsonProperty("variables") Map<String, Object> variables) {
		this.query = query;
		this.operationName = operationName;
		this.variables = variables;
	}
}
