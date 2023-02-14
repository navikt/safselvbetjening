package no.nav.safselvbetjening.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemaTest {

	@Test
	void shouldVerifyBrukerHarInnsyn() {
		assertThat(Tema.brukerHarInnsyn()).hasSize(57);
	}

	@Test
	void shouldVerifyBrukerHarInnsynAsListString() {
		assertThat(Tema.brukerHarInnsynAsListString()).hasSize(57);
		assertThat(Tema.brukerHarInnsynAsListString()).doesNotContain(Tema.brukerHarIkkeInnsynAsString().toArray(String[]::new));
	}

	@Test
	void shouldVerifyBrukerHarIkkeInnsyn() {
		assertThat(Tema.brukerHarIkkeInnsyn()).hasSize(5);
	}

	@Test
	void shouldVerifyBrukerHarIkkeInnsynAsString() {
		assertThat(Tema.brukerHarIkkeInnsynAsString()).hasSize(5);
	}
}