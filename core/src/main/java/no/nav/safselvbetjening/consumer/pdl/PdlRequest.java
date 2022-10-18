package no.nav.safselvbetjening.consumer.pdl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record PdlRequest(
		String query,
		Map<String, String> variables
) {

	@JsonCreator
	public PdlRequest(
			@JsonProperty("query") String query,
			@JsonProperty("variables") Map<String, String> variables) {
		this.query = query;
		this.variables = variables;
	}
}
