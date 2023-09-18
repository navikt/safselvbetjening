package no.nav.safselvbetjening.dokumentoversikt;

import graphql.schema.DataFetchingEnvironmentImpl;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningDataFetcher.temaArgumentEllerFullmakt;
import static org.assertj.core.api.Assertions.assertThat;

class DokumentoversiktSelvbetjeningDataFetcherTest {

	@Test
	void shouldReturnFullmaktTemaWhenTemaArgumentNotGiven() {
		var environment = new DataFetchingEnvironmentImpl.Builder().build();
		var fullmakt = Optional.of(new Fullmakt("11111111111", "22222222222", List.of("FOR", "AAP")));

		List<String> tema = temaArgumentEllerFullmakt(environment, fullmakt);

		assertThat(tema).contains("FOR", "AAP");
	}

	@Test
	void shouldReturnOnlyTemaArgumentWhenFullmaktHasMultipleTema() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		var fullmakt = Optional.of(new Fullmakt("11111111111", "22222222222", List.of("FOR", "AAP")));

		List<String> tema = temaArgumentEllerFullmakt(environment, fullmakt);

		assertThat(tema).contains("FOR");
	}

	@Test
	void shouldReturnNoTemaWhenFullmaktDoesNotMatchArgument() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		var fullmakt = Optional.of(new Fullmakt("11111111111", "22222222222", List.of("AAP", "BAR")));

		List<String> tema = temaArgumentEllerFullmakt(environment, fullmakt);

		assertThat(tema).hasSize(0);
	}

	@Test
	void shouldReturnTemaArgumentWhenNoFullmakt() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		Optional<Fullmakt> fullmakt = Optional.empty();

		List<String> tema = temaArgumentEllerFullmakt(environment, fullmakt);

		assertThat(tema).contains("FOR");
	}
}