package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import org.junit.jupiter.api.Test;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static org.assertj.core.api.Assertions.assertThat;

class TilgangJournalpostDtoTest {
	@Test
	void shouldReturnTrueWhenTilknyttetPensjonsak() {
		TilgangJournalpostDto tilgangJournalpostDto = TilgangJournalpostDto.builder().sak(TilgangSakDto.builder().fagsystem(PEN.name()).build()).build();

		assertThat(tilgangJournalpostDto.isTilknyttetPensjonsak()).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNotTilknyttetPensjonsak() {
		TilgangJournalpostDto tilgangJournalpostDto = TilgangJournalpostDto.builder().sak(TilgangSakDto.builder().fagsystem(FS22.name()).build()).build();

		assertThat(tilgangJournalpostDto.isTilknyttetPensjonsak()).isFalse();
	}
}