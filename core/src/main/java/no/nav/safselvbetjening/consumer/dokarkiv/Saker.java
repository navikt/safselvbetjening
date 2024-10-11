package no.nav.safselvbetjening.consumer.dokarkiv;

import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.consumer.sak.Joarksak;
import no.nav.safselvbetjening.domain.Fagsak;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostMapper.FAGSYSTEM_PENSJON;
import static no.nav.safselvbetjening.domain.Sakstype.APPLIKASJON_GENERELL_SAK;
import static org.apache.commons.lang3.StringUtils.isBlank;

public record Saker(
		List<Joarksak> arkivsaker,
		List<Pensjonsak> pensjonsaker
) {
	public Stream<String> getArkivsakerTemaer() {
		return Stream.concat(
				arkivsaker.stream().map(Joarksak::getTema),
				pensjonsaker.stream().map(Pensjonsak::arkivtema)
		);
	}

	public Stream<Fagsak> getFagsaker() {
		return Stream.concat(
				arkivsaker.stream().map(Saker::mapFagsak),
				pensjonsaker.stream().map(Saker::mapFagsak)
		).filter(Saker::isNotGenerellSak);
	}

	private static boolean isNotGenerellSak(Fagsak fagsak) {
		if (isBlank(fagsak.getFagsakId()) && isBlank(fagsak.getFagsaksystem())) {
			return false;
		}
		return !APPLIKASJON_GENERELL_SAK.equals(fagsak.getFagsaksystem());
	}

	static Fagsak mapFagsak(Joarksak arkivsak) {
		return Fagsak.builder()
				.fagsakId(String.valueOf(arkivsak.getId()))
				.fagsaksystem(arkivsak.getApplikasjon())
				.tema(arkivsak.getTema())
				.build();
	}

	static Fagsak mapFagsak(Pensjonsak arkivsak) {
		return Fagsak.builder()
				.fagsakId(String.valueOf(arkivsak.sakId()))
				.fagsaksystem(FAGSYSTEM_PENSJON)
				.tema(arkivsak.arkivtema())
				.build();
	}
}
