package no.nav.safselvbetjening.dokumentoversikt;

import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterRequestTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.service.SakService;
import no.nav.safselvbetjening.service.Saker;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static no.nav.safselvbetjening.graphql.ErrorCode.NOT_FOUND;

@Slf4j
@Component
class DokumentoversiktSelvbetjeningService {
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final IdentService identService;
	private final SakService sakService;
	private final FagarkivConsumer fagarkivConsumer;

	public DokumentoversiktSelvbetjeningService(final SafSelvbetjeningProperties safSelvbetjeningProperties,
												final IdentService identService,
												final SakService sakService,
												final FagarkivConsumer fagarkivConsumer,
												final JournalpostMapper journalpostMapper,
												final UtledTilgangService utledTilgangService) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.identService = identService;
		this.sakService = sakService;
		this.fagarkivConsumer = fagarkivConsumer;
	}

	Basedata queryBasedata(final String ident, final List<String> tema, DataFetchingEnvironment environment) {
		final BrukerIdenter brukerIdenter = identService.hentIdenter(ident);
		if (brukerIdenter.isEmpty()) {
			throw GraphQLException.of(NOT_FOUND, environment, "Finner ingen identer på person.");
		}
		final Saker saker = sakService.hentSaker(brukerIdenter, tema);
		return new Basedata(brukerIdenter, saker);
	}

	List<JournalpostDto> queryBaseJournalposter(final Basedata basedata) {
		final BrukerIdenter brukerIdenter = basedata.getBrukerIdenter();
		final Saker saker = basedata.getSaker();
		/*
		 * Regler tilgangskontroll journalpost: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
		 * 1b) Bruker får ikke se journalposter som er opprettet før 04.06.2016
		 * 1c) Bruker får kun se ferdigstilte journalposter
		 * 1d) Bruker får ikke se feilregistrerte journalposter
		 */
		return fagarkivConsumer.finnJournalposter(FinnJournalposterRequestTo.builder()
				.alleIdenter(brukerIdenter.getFoedselsnummer())
				.psakSakIds(saker.getPensjonSakIds())
				.gsakSakIds(saker.getArkivSakIds())
				.fraDato(safSelvbetjeningProperties.getTidligstInnsynDato().toString())
				.inkluderJournalpostType(Arrays.asList(JournalpostTypeCode.values()))
				.inkluderJournalStatus(Arrays.asList(JournalStatusCode.MO, JournalStatusCode.M, JournalStatusCode.J, JournalStatusCode.E, JournalStatusCode.FL, JournalStatusCode.FS))
				.foerste(9999)
				.visFeilregistrerte(false)
				.build()).getTilgangJournalposter();
	}

}
