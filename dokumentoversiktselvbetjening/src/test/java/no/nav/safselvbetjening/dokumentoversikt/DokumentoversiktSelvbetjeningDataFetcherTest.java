package no.nav.safselvbetjening.dokumentoversikt;

import graphql.schema.DataFetchingEnvironmentImpl;
import no.nav.safselvbetjening.representasjon.Fullmakt;
import no.nav.safselvbetjening.representasjon.Representasjon;
import no.nav.safselvbetjening.representasjon.Vergemaal;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningDataFetcher.temaArgumentEllerRepresentasjon;
import static org.assertj.core.api.Assertions.assertThat;

class DokumentoversiktSelvbetjeningDataFetcherTest {

	@Test
	void shouldReturnVergemaalAndFullmaktTemaWhenTemaArgumentNotGiven() {
		var environment = new DataFetchingEnvironmentImpl.Builder().build();
		var representasjon = Optional.of(new Representasjon(List.of(createFullmakt(Set.of("FOR")), createVergemaal(Set.of("AAP")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).contains("FOR", "AAP");
	}

	@Test
	void shouldReturnFullmaktTemaWhenTemaArgumentNotGiven() {
		var environment = new DataFetchingEnvironmentImpl.Builder().build();
		var representasjon = Optional.of(new Representasjon(List.of(createFullmakt(Set.of("FOR", "AAP")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).contains("FOR", "AAP");
	}

	@Test
	void shouldReturnOnlyTemaArgumentWhenFullmaktHasMultipleTema() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		var representasjon = Optional.of(new Representasjon(List.of(createFullmakt(Set.of("FOR", "AAP")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).contains("FOR");
	}

	@Test
	void shouldReturnNoTemaWhenFullmaktDoesNotMatchArgument() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		var representasjon = Optional.of(new Representasjon(List.of(createFullmakt(Set.of("AAP", "BAR")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).hasSize(0);
	}

	@Test
	void shouldReturnTemaArgumentWhenNoRepresentasjon() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		Optional<Representasjon> representasjon = Optional.empty();

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).contains("FOR");
	}

	@Test
	void shouldReturnVergemaalTemaWhenTemaArgumentNotGiven() {
		var environment = new DataFetchingEnvironmentImpl.Builder().build();
		var representasjon = Optional.of(new Representasjon(List.of(createVergemaal(Set.of("FOR", "AAP")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).contains("FOR", "AAP");
	}

	@Test
	void shouldReturnOnlyTemaArgumentWhenVergemaalHasMultipleTema() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		var representasjon = Optional.of(new Representasjon(List.of(createVergemaal(Set.of("FOR", "AAP")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).contains("FOR");
	}

	@Test
	void shouldReturnNoTemaWhenVergemaalDoesNotMatchArgument() {
		var environment = new DataFetchingEnvironmentImpl.Builder()
				.arguments(Map.of("tema", List.of("FOR")))
				.build();
		var representasjon = Optional.of(new Representasjon(List.of(createVergemaal(Set.of("AAP", "BAR")))));

		List<String> tema = temaArgumentEllerRepresentasjon(environment, representasjon);

		assertThat(tema).hasSize(0);
	}

	private static Fullmakt createFullmakt(Set<String> tema) {
		return new Fullmakt("11111111111", "22222222222", tema);
	}

	private static Vergemaal createVergemaal(Set<String> tema) {
		return new Vergemaal("11111111111", "22222222222", tema);
	}
}