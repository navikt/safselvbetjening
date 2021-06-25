package no.nav.safselvbetjening.dokumentoversikt;

import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterRequestTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.service.SakService;
import no.nav.safselvbetjening.service.Saker;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;
import static no.nav.safselvbetjening.graphql.ErrorCode.NOT_FOUND;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.STATUS_OK;

@Slf4j
@Component
class DokumentoversiktSelvbetjeningService {
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final IdentService identService;
	private final SakService sakService;
	private final FagarkivConsumer fagarkivConsumer;
	private final JournalpostMapper journalpostMapper;
	private final UtledTilgangService utledTilgangService;

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
		this.journalpostMapper = journalpostMapper;
		this.utledTilgangService = utledTilgangService;
	}

	Basedata queryBasedata(final String ident, final List<String> tema, final DataFetchingEnvironment environment) {
		final BrukerIdenter brukerIdenter = identService.hentIdenter(ident);
		if (brukerIdenter.isEmpty()) {
			throw GraphQLException.of(NOT_FOUND, environment, "Finner ingen identer på person.");
		}
		final Saker saker = sakService.hentSaker(brukerIdenter, tema);
		return new Basedata(brukerIdenter, saker);
	}

	/*
	 * Henter og filtrerer journalposter etter tilgangsreglene i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
	 */
	Journalpostdata queryFiltrerteJournalposter(final Basedata basedata, final List<String> tema) {
		final BrukerIdenter brukerIdenter = basedata.getBrukerIdenter();
		final Saker saker = basedata.getSaker();
		/*
		 * 1b) Bruker får ikke se journalposter som er opprettet før 04.06.2016
		 * 1c) Bruker får kun se ferdigstilte journalposter
		 * 1d) Bruker får ikke se feilregistrerte journalposter
		 */
		List<JournalpostDto> tilgangJournalposter = fagarkivConsumer.finnJournalposter(finnjournalposterRequest(brukerIdenter, saker)).getTilgangJournalposter();
		List<Journalpost> filtrerteJournalposter = tilgangJournalposter
				.stream()
				.map(journalpostDto -> journalpostMapper.map(journalpostDto, saker, brukerIdenter))
				.filter(Objects::nonNull)
				.filter(journalpost -> utledTilgangService.utledTilgangJournalpost(journalpost, brukerIdenter))
				.map(journalpost -> setDokumentVariant(journalpost, brukerIdenter))
				// Filtrer ut midlertidige journalposter som ikke har riktig tema.
				.filter(journalpost -> tema.contains(journalpost.getTema()))
				.collect(toList());
		return new Journalpostdata(tilgangJournalposter.size(), filtrerteJournalposter);
	}

	private FinnJournalposterRequestTo finnjournalposterRequest(BrukerIdenter brukerIdenter, Saker saker) {
		return FinnJournalposterRequestTo.builder()
				.alleIdenter(brukerIdenter.getFoedselsnummer())
				.psakSakIds(saker.getPensjonSakIds())
				.gsakSakIds(saker.getArkivSakIds())
				.fraDato(safSelvbetjeningProperties.getTidligstInnsynDato().toString())
				.inkluderJournalpostType(Arrays.asList(JournalpostTypeCode.values()))
				.inkluderJournalStatus(Arrays.asList(MO, M, J, E, FL, FS))
				.foerste(9999)
				.visFeilregistrerte(false)
				.build();
	}

	private Journalpost setDokumentVariant(Journalpost journalpost, BrukerIdenter brukerIdenter) {
		journalpost.getDokumenter().forEach(dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
				dokumentvariant -> {
					List<String> codes = utledTilgangService.utledTilgangDokument(journalpost, dokumentInfo, dokumentvariant, brukerIdenter);
					dokumentvariant.setBrukerHarTilgang(codes.isEmpty());
					dokumentvariant.setCode(codes.isEmpty() ? singletonList(STATUS_OK) : codes);
				}));
		return journalpost;
	}
}
