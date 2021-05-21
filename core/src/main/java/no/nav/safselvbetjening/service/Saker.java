package no.nav.safselvbetjening.service;

import lombok.Getter;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.consumer.sak.Arkivsak;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
public class Saker {
    private final List<Sak> arkivsaker;
    private final List<Sak> pensjonsaker;


    Saker(final List<Arkivsak> arkivsaker, final List<Pensjonsak> pensjonsaker) {
        this.arkivsaker = arkivsaker.stream()
                .map(s -> Sak.builder().arkivsakId(s.getId().toString()).tema(s.getTema()).build())
                .collect(Collectors.toList());
        this.pensjonsaker = pensjonsaker.stream()
                .map(s -> Sak.builder().arkivsakId(s.getSakNr()).tema(s.getTema()).build())
                .collect(Collectors.toList());
    }

    public List<String> getArkivSakIds() {
        return arkivsaker.stream()
                .map(Sak::getArkivsakId).collect(Collectors.toUnmodifiableList());
    }

    public List<String> getPensjonSakIds() {
        return pensjonsaker.stream()
                .map(Sak::getArkivsakId).collect(Collectors.toUnmodifiableList());
    }
}
