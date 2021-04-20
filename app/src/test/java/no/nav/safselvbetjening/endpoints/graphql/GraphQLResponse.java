package no.nav.safselvbetjening.endpoints.graphql;

import lombok.Data;
import no.nav.safselvbetjening.domain.Dokumentoversikt;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class GraphQLResponse {
	private DataWrapper data;
	private List<Error> errors;

	@Data
	public static class DataWrapper {
		private Dokumentoversikt dokumentoversiktSelvbetjening;
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
