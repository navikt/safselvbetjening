package no.nav.safselvbetjening.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemaTest {

	@Test
	void shouldVerifyBrukerHarInnsyn() {
		assertThat(Tema.tillattInnsynNavNo()).hasSize(61);
	}

	@Test
	void shouldVerifyBrukerHarInnsynAsListString() {
		assertThat(Tema.tillattInnsynNavNoString()).hasSize(61);
		assertThat(Tema.tillattInnsynNavNoString()).doesNotContain(Tema.unntattInnsynNavNoString().toArray(String[]::new));
	}

	@Test
	void shouldVerifyBrukerHarIkkeInnsyn() {
		assertThat(Tema.unntattInnsynNavNo()).hasSize(6);
	}

	@Test
	void shouldVerifyBrukerHarIkkeInnsynAsString() {
		assertThat(Tema.unntattInnsynNavNoString()).hasSize(6);
	}
}