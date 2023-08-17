package no.nav.safselvbetjening.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class JournalpostTest {
	@Test
	void shouldMapTilgangJournalpostGjeldendeTemaWhenSakstilknytning() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.tema(Tema.AAP.name())
				.tilgangSak(Journalpost.TilgangSak.builder()
						.tema(Tema.DAG.name())
						.build())
				.build();
		assertThat(tilgangJournalpost.getGjeldendeTema()).isEqualTo(Tema.DAG.name());
	}

	@Test
	void shouldMapTilgangJournalpostGjeldendeTemaWhenNoSakstilknytning() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.tema(Tema.AAP.name())
				.tilgangSak(null)
				.build();
		assertThat(tilgangJournalpost.getGjeldendeTema()).isEqualTo(Tema.AAP.name());
	}

	@Test
	void shouldMapTilgangJournalpostGjeldendeTemaWhenSakstilknytningTemaNull() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.tema(Tema.AAP.name())
				.tilgangSak(Journalpost.TilgangSak.builder()
						.tema(null)
						.build())
				.build();
		assertThat(tilgangJournalpost.getGjeldendeTema()).isEqualTo(Tema.AAP.name());
	}

	@Test
	void shouldReturnTrueWhenGjeldendeTemaIsUnntattInnsyn() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.tema(Tema.KTA.name())
				.tilgangSak(Journalpost.TilgangSak.builder()
						.tema(Tema.KTA.name())
						.build())
				.build();
		assertThat(tilgangJournalpost.isGjeldendeTemaUnntattInnsyn()).isTrue();
	}

	@Test
	void shouldReturnFalseWhenGjeldendeTemaIsNotUnntattInnsyn() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.tema(Tema.DAG.name())
				.tilgangSak(Journalpost.TilgangSak.builder()
						.tema(Tema.DAG.name())
						.build())
				.build();
		assertThat(tilgangJournalpost.isGjeldendeTemaUnntattInnsyn()).isFalse();
	}

	@Test
	void shouldReturnFalseForInnsynSkjulesWhenNull() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.innsyn(null)
				.build();
		assertThat(tilgangJournalpost.innsynSkjules()).isFalse();
	}

	@Test
	void shouldReturnFalseForInnsynVisesWhenNull() {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.innsyn(null)
				.build();
		assertThat(tilgangJournalpost.innsynVises()).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Innsyn.class, names = {"SKJULES_BRUKERS_ONSKE", "SKJULES_INNSKRENKET_PARTSINNSYN", "SKJULES_FEILSENDT", "SKJULES_ORGAN_INTERNT"})
	void shouldReturnTrueForInnsynSkjulesWhenSkjules(Innsyn innsyn) {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynSkjules()).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Innsyn.class, names = {"SKJULES_BRUKERS_ONSKE", "SKJULES_INNSKRENKET_PARTSINNSYN", "SKJULES_FEILSENDT", "SKJULES_ORGAN_INTERNT"})
	void shouldReturnFalseForInnsynSkjulesWhenVises(Innsyn innsyn) {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynVises()).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Innsyn.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT", "VISES_FORVALTNINGSNOTAT"})
	void shouldReturnTrueForInnsynVisesWhenVises(Innsyn innsyn) {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynVises()).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Innsyn.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT", "VISES_FORVALTNINGSNOTAT"})
	void shouldReturnFalseForInnsynVisesWhenSkjules(Innsyn innsyn) {
		Journalpost.TilgangJournalpost tilgangJournalpost = Journalpost.TilgangJournalpost.builder()
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynSkjules()).isFalse();
	}
}