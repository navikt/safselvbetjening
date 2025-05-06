package no.nav.safselvbetjening.tilgang;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.BRUK_STANDARDREGLER;
import static org.assertj.core.api.Assertions.assertThat;

class TilgangJournalpostTest {
	@Test
	void shouldMapTilgangJournalpostGjeldendeTemaWhenSakstilknytning() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.tema("AAP")
				.tilgangSak(TilgangSak.builder()
						.tema("DAG")
						.build())
				.build();
		assertThat(tilgangJournalpost.getGjeldendeTema()).isEqualTo("DAG");
	}

	@Test
	void shouldMapTilgangJournalpostGjeldendeTemaWhenNoSakstilknytning() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.tema("AAP")
				.tilgangSak(null)
				.build();
		assertThat(tilgangJournalpost.getGjeldendeTema()).isEqualTo("AAP");
	}

	@Test
	void shouldMapTilgangJournalpostGjeldendeTemaWhenSakstilknytningTemaNull() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.innsyn(BRUK_STANDARDREGLER)
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.tema("AAP")
				.tilgangSak(TilgangSak.builder()
						.tema(null)
						.build())
				.build();
		assertThat(tilgangJournalpost.getGjeldendeTema()).isEqualTo("AAP");
	}

	@Test
	void shouldReturnFalseForInnsynSkjulesWhenStandardregler() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(BRUK_STANDARDREGLER)
				.build();
		assertThat(tilgangJournalpost.innsynSkjules()).isFalse();
	}

	@Test
	void shouldReturnFalseForInnsynVisesWhenStandardregler() {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(BRUK_STANDARDREGLER)
				.build();
		assertThat(tilgangJournalpost.innsynVises()).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = TilgangInnsyn.class, names = {
			"SKJULES_BRUKERS_ONSKE", "SKJULES_INNSKRENKET_PARTSINNSYN", "SKJULES_FEILSENDT", "SKJULES_ORGAN_INTERNT", "SKJULES_BRUKERS_SIKKERHET"
	})
	void shouldReturnTrueForInnsynSkjulesWhenSkjules(TilgangInnsyn innsyn) {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynSkjules()).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = TilgangInnsyn.class, names = {
			"SKJULES_BRUKERS_ONSKE", "SKJULES_INNSKRENKET_PARTSINNSYN", "SKJULES_FEILSENDT", "SKJULES_ORGAN_INTERNT", "SKJULES_BRUKERS_SIKKERHET"
	})
	void shouldReturnFalseForInnsynSkjulesWhenVises(TilgangInnsyn innsyn) {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynVises()).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = TilgangInnsyn.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT", "VISES_FORVALTNINGSNOTAT"})
	void shouldReturnTrueForInnsynVisesWhenVises(TilgangInnsyn innsyn) {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynVises()).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = TilgangInnsyn.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT", "VISES_FORVALTNINGSNOTAT"})
	void shouldReturnFalseForInnsynVisesWhenSkjules(TilgangInnsyn innsyn) {
		TilgangJournalpost tilgangJournalpost = TilgangJournalpost.builder()
				.datoOpprettet(LocalDateTime.now())
				.mottakskanal(TilgangMottakskanal.IKKE_SKANNING_IKKE_TEKNISK)
				.utsendingskanal(TilgangUtsendingskanal.IKKE_TEKNISK)
				.innsyn(innsyn)
				.build();
		assertThat(tilgangJournalpost.innsynSkjules()).isFalse();
	}
}