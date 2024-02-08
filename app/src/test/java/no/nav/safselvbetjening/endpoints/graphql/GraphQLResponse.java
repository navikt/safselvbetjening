package no.nav.safselvbetjening.endpoints.graphql;

import lombok.Data;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Journalpost;

import java.util.List;

@Data
public class GraphQLResponse {
	private DataWrapper data;
	private List<Error> errors;

	@Data
	public static class DataWrapper {
		private Dokumentoversikt dokumentoversiktSelvbetjening;
		private Journalpost journalpostById;
	}

	@Data
	public static class Error {
		private Extensions extensions;
	}

	@Data
	public static class Extensions {
		private String code;
	}
}
