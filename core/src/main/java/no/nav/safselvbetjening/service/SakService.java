package no.nav.safselvbetjening.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.dokarkiv.Saker;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakRestConsumer;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.consumer.sak.Joarksak;
import no.nav.safselvbetjening.consumer.sak.SakConsumer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

/**
 * Tjeneste som konsoliderer arkivsaker fra fagarkivet og pensjonssaker.
 */
@Slf4j
@Component
public class SakService {

	private static final List<String> TEMA_PENSJON = Arrays.asList("UFO", "PEN");
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final SakConsumer sakConsumer;

	public SakService(PensjonSakRestConsumer pensjonSakRestConsumer, SakConsumer sakConsumer) {
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.sakConsumer = sakConsumer;
	}

	public Saker hentSaker(BrukerIdenter brukerIdenter, List<String> tema) {
		List<Joarksak> arkivsaker = hentArkivsaker(brukerIdenter.getAktoerIds(), tema);
		List<Pensjonsak> pensjonsaker = hentPensjonssaker(brukerIdenter.getAktivFolkeregisterident(), tema);
		return new Saker(arkivsaker, pensjonsaker);
	}

	private List<Joarksak> hentArkivsaker(List<String> aktoerIds, List<String> tema) {
		try {
			return sakConsumer.hentSaker(aktoerIds, tema);
		} catch (ConsumerFunctionalException e) {
			log.warn("Henting av arkivsaker feilet.", e);
			return emptyList();
		} catch (ConsumerTechnicalException e) {
			log.error("Henting av arkivsaker feilet teknisk.", e);
			return emptyList();
		}
	}

	private List<Pensjonsak> hentPensjonssaker(String aktivFolkeregisterident, List<String> tema) {
		if (Collections.disjoint(tema, TEMA_PENSJON)) {
			return emptyList();
		}
		try {
			return pensjonSakRestConsumer.hentPensjonssaker(aktivFolkeregisterident)
					.stream()
					.filter(sak -> nonNull(sak.arkivtema()))
					.toList();
		} catch (ConsumerFunctionalException e) {
			log.warn("Henting av pensjonssaker feilet.", e);
			return emptyList();
		} catch (Exception e) {
			log.error("Henting av pensjonssaker feilet teknisk.", e);
			return emptyList();
		}
	}
}
