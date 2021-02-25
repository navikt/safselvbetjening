package no.nav.safselvbetjening.service;

import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakWsConsumer;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.consumer.sak.Arkivsak;
import no.nav.safselvbetjening.consumer.sak.ArkivsakConsumer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SakService {
    private static final List<String> TEMA_PENSJON = Arrays.asList("UFO", "PEN");
    private final PensjonSakWsConsumer pensjonSakWsConsumer;
    private final ArkivsakConsumer arkivsakConsumer;

    public SakService(PensjonSakWsConsumer pensjonSakWsConsumer, ArkivsakConsumer arkivsakConsumer) {
        this.pensjonSakWsConsumer = pensjonSakWsConsumer;
        this.arkivsakConsumer = arkivsakConsumer;
    }

    public List<Sak> hentSaker(BrukerIdenter brukerIdenter, final List<String> tema) {
        List<Sak> arkivsaker = hentArkivsaker(brukerIdenter.getAktoerIds(), tema);
        List<Sak> pensjonsaker = hentPensjonSaker(brukerIdenter.getFoedselsnummer(), tema);
        List<Sak> saker = new ArrayList<>();
        saker.addAll(arkivsaker);
        saker.addAll(pensjonsaker);
        return saker;
    }

    private List<Sak> hentArkivsaker(final List<String> aktoerIds, final List<String> tema) {
        try {
            final List<Arkivsak> arkivsaker = arkivsakConsumer.hentSaker(aktoerIds, tema);
            if (arkivsaker.isEmpty()) {
                return new ArrayList<>();
            }
            return arkivsaker.stream()
                    .map(s -> Sak.builder().arkivsakId(s.getId().toString()).tema(s.getTema()).build())
                    .collect(Collectors.toList());
        } catch (ConsumerFunctionalException | ConsumerTechnicalException e) {
            return new ArrayList<>();
        }
    }

    private List<Sak> hentPensjonSaker(final List<String> identer, final List<String> tema) {
        if (Collections.disjoint(tema, TEMA_PENSJON)) {
            return new ArrayList<>();
        }
        try {
            final List<Pensjonsak> allePensjonSaker = new ArrayList<>();
            for (String ident : identer) {
                List<Pensjonsak> pensjonsaker = pensjonSakWsConsumer.hentSakSammendragListe(ident);
                allePensjonSaker.addAll(pensjonsaker);
            }
            return allePensjonSaker.stream()
                    .map(s -> Sak.builder().arkivsakId(s.getSakNr()).tema(s.getTema()).build())
                    .collect(Collectors.toList());
        } catch (ConsumerFunctionalException | ConsumerTechnicalException e) {
            return new ArrayList<>();
        }
    }
}
