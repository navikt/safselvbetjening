package no.nav.safselvbetjening.service;

import lombok.extern.slf4j.Slf4j;
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

/**
 * Tjeneste som konsoliderer arkivsaker fra fagarkivet og pensjonssaker.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class SakService {
    private static final List<String> TEMA_PENSJON = Arrays.asList("UFO", "PEN");
    private final PensjonSakWsConsumer pensjonSakWsConsumer;
    private final ArkivsakConsumer arkivsakConsumer;

    public SakService(PensjonSakWsConsumer pensjonSakWsConsumer, ArkivsakConsumer arkivsakConsumer) {
        this.pensjonSakWsConsumer = pensjonSakWsConsumer;
        this.arkivsakConsumer = arkivsakConsumer;
    }

    public Saker hentSaker(BrukerIdenter brukerIdenter, final List<String> tema) {
        List<Arkivsak> arkivsaker = hentArkivsaker(brukerIdenter.getAktoerIds(), tema);
        List<Pensjonsak> pensjonsaker = hentPensjonSaker(brukerIdenter.getFoedselsnummer(), tema);
        return new Saker(arkivsaker, pensjonsaker);
    }

    private List<Arkivsak> hentArkivsaker(final List<String> aktoerIds, final List<String> tema) {
        try {
            return arkivsakConsumer.hentSaker(aktoerIds, tema);
        } catch (ConsumerFunctionalException | ConsumerTechnicalException e) {
            log.warn("Henting av arkivsaker feilet. ", e);
            return new ArrayList<>();
        }
    }

    private List<Pensjonsak> hentPensjonSaker(final List<String> identer, final List<String> tema) {
        if (Collections.disjoint(tema, TEMA_PENSJON)) {
            return new ArrayList<>();
        }
        try {
            final List<Pensjonsak> allePensjonSaker = new ArrayList<>();
            for (String ident : identer) {
                List<Pensjonsak> pensjonsaker = pensjonSakWsConsumer.hentSakSammendragListe(ident);
                allePensjonSaker.addAll(pensjonsaker);
            }
            return allePensjonSaker;
        } catch (ConsumerFunctionalException | ConsumerTechnicalException e) {
            log.warn("Henting av pensjonssaker feilet. ", e);
            return new ArrayList<>();
        }
    }
}
