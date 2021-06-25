package no.nav.safselvbetjening.service;

import lombok.Getter;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.consumer.sak.Joarksak;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
public class Saker {
	public static final String FAGSYSTEM_PENSJON = "PP01";
	private final List<Arkivsak> joarksaker;
	private final List<Arkivsak> pensjonsaker;
	private final Map<String, String> arkivsakIdTemaMap;

	public Saker(final List<Joarksak> joarksaker, final List<Pensjonsak> pensjonsaker) {
		this.joarksaker = joarksaker.stream()
				.map(s -> Arkivsak.builder()
						.arkivsakId(s.getId().toString())
						.tema(s.getTema())
						.fagsakId(s.getFagsakNr())
						.fagsaksystem(s.getApplikasjon())
						.build())
				.collect(toList());
		this.pensjonsaker = pensjonsaker.stream()
				.map(s -> Arkivsak.builder()
						.arkivsakId(s.getSakNr())
						.tema(s.getTema())
						.fagsakId(s.getSakNr())
						.fagsaksystem(FAGSYSTEM_PENSJON)
						.build())
				.collect(toList());
		arkivsakIdTemaMap = getArkivsakerAsStream()
				.collect(toMap(Arkivsak::getArkivsakId, Arkivsak::getTema));
	}

	public Stream<Arkivsak> getArkivsakerAsStream() {
		return concat(joarksaker.stream(), pensjonsaker.stream());
	}

	public List<String> getArkivSakIds() {
		return joarksaker.stream()
				.map(Arkivsak::getArkivsakId).collect(Collectors.toUnmodifiableList());
	}

	public List<String> getPensjonSakIds() {
		return pensjonsaker.stream()
				.map(Arkivsak::getArkivsakId).collect(Collectors.toUnmodifiableList());
	}
}
