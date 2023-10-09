package no.nav.safselvbetjening.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSaksrelasjon;
import no.nav.safselvbetjening.consumer.pdl.IdentConsumer;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakRestConsumer;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class IdentService {
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final IdentConsumer identConsumer;

	public IdentService(PensjonSakRestConsumer pensjonSakRestConsumer, IdentConsumer identConsumer) {
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.identConsumer = identConsumer;
	}

	public BrukerIdenter hentIdenter(ArkivJournalpost arkivJournalpost) {
		if(arkivJournalpost.isTilknyttetSak()) {
			return mapBrukerIdenterSakstilknytning(arkivJournalpost);
		} else {
			return mapBrukerIdenterMidlertidig(arkivJournalpost);
		}
	}

	private BrukerIdenter mapBrukerIdenterSakstilknytning(ArkivJournalpost arkivJournalpost) {
		ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
		if(arkivSaksrelasjon.isPensjonsak()) {
			String pensjonFnr = pensjonSakRestConsumer.hentBrukerForSak(arkivSaksrelasjon.sakId().toString()).fnr();
			return hentIdenter(pensjonFnr);
		} else {
			return hentIdenter(arkivSaksrelasjon.sak().aktoerId());
		}
	}

	private BrukerIdenter mapBrukerIdenterMidlertidig(ArkivJournalpost arkivJournalpost) {
		if(arkivJournalpost.bruker() == null) {
			return BrukerIdenter.empty();
		} else {
			return hentIdenter(arkivJournalpost.bruker().id());
		}
	}

	public BrukerIdenter hentIdenter(final String ident) {
		try {
			if(isBlank(ident)) {
				return BrukerIdenter.empty();
			}

			List<PdlResponse.PdlIdent> pdlIdenter = identConsumer.hentIdenter(ident);
			return new BrukerIdenter(pdlIdenter);
		} catch (ConsumerFunctionalException e) {
			log.warn("Henting av identer for ident feilet.", e);
			return BrukerIdenter.empty();
		}
	}
}
