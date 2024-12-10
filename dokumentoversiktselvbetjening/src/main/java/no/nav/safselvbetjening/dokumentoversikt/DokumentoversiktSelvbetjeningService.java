package no.nav.safselvbetjening.dokumentoversikt;

import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.dokarkiv.Basedata;
import no.nav.safselvbetjening.consumer.dokarkiv.DokarkivConsumer;
import no.nav.safselvbetjening.consumer.dokarkiv.Saker;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostMapper;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSaksrelasjon;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.FinnJournalposterRequest;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.consumer.sak.Joarksak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.service.SakService;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.MO;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_IKKE_FUNNET_I_PDL;
import static no.nav.safselvbetjening.graphql.ErrorCode.NOT_FOUND;

@Slf4j
@Component
class DokumentoversiktSelvbetjeningService {

	private static final List<JournalStatusCode> MIDLERTIDIGE_OG_FERDIGSTILTE_JOURNALSTATUSER = Arrays.asList(MO, M, J, E, FL, FS);
	private static final List<JournalStatusCode> FERDIGSTILTE_JOURNALSTATUSER = Arrays.asList(J, E, FL, FS);
	private static final List<JournalpostTypeCode> ALLE_JOURNALPOSTTYPER = Arrays.asList(JournalpostTypeCode.values());

	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final IdentService identService;
	private final SakService sakService;
	private final DokarkivConsumer dokarkivConsumer;
	private final ArkivJournalpostMapper arkivJournalpostMapper;
	private final UtledTilgangService utledTilgangService;

	public DokumentoversiktSelvbetjeningService(SafSelvbetjeningProperties safSelvbetjeningProperties,
												IdentService identService,
												SakService sakService,
												DokarkivConsumer dokarkivConsumer,
												ArkivJournalpostMapper arkivJournalpostMapper,
												UtledTilgangService utledTilgangService) {
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.identService = identService;
		this.sakService = sakService;
		this.dokarkivConsumer = dokarkivConsumer;
		this.arkivJournalpostMapper = arkivJournalpostMapper;
		this.utledTilgangService = utledTilgangService;
	}

	Basedata queryBasedata(final String ident, final List<String> tema, final DataFetchingEnvironment environment) {
		final BrukerIdenter brukerIdenter = identService.hentIdenter(ident);
		if (brukerIdenter.isEmpty()) {
			throw GraphQLException.of(NOT_FOUND, environment, FEILMELDING_BRUKER_IKKE_FUNNET_I_PDL);
		}
		final Saker saker = sakService.hentSaker(brukerIdenter, tema);
		return new Basedata(brukerIdenter, saker);
	}

	Journalpostdata queryFiltrerAlleJournalposter(Basedata basedata, List<String> tema, Map<Long, Pensjonsak> pensjonsaker) {
		final BrukerIdenter brukerIdenter = basedata.brukerIdenter();
		final Saker saker = basedata.saker();
		List<ArkivJournalpost> tilgangJournalposter = new ArrayList<>();
		if(!saker.arkivsaker().isEmpty()) {
			tilgangJournalposter.addAll(dokarkivConsumer.finnJournalposter(finnAlleJournalposterRequest(brukerIdenter, saker), emptySet()).journalposter());
		}
		if(!saker.pensjonsaker().isEmpty()) {
			tilgangJournalposter.addAll(dokarkivConsumer.finnJournalposter(finnPensjonJournalposterRequest(saker, MIDLERTIDIGE_OG_FERDIGSTILTE_JOURNALSTATUSER, brukerIdenter.getFoedselsnummer()), emptySet()).journalposter());
		}
		return mapOgFiltrerJournalposter(tema, brukerIdenter, pensjonsaker, tilgangJournalposter);
	}

	Journalpostdata queryFiltrerSakstilknyttedeJournalposter(Basedata basedata, List<String> tema, Map<Long, Pensjonsak> pensjonsaker) {
		final BrukerIdenter brukerIdenter = basedata.brukerIdenter();
		final Saker saker = basedata.saker();
		List<ArkivJournalpost> tilgangJournalposter = new ArrayList<>();
		if(!saker.arkivsaker().isEmpty()) {
			tilgangJournalposter.addAll(dokarkivConsumer.finnJournalposter(finnFerdigstilteJournalposterRequest(saker), emptySet()).journalposter());
		}
		if(!saker.pensjonsaker().isEmpty()) {
			tilgangJournalposter.addAll(dokarkivConsumer.finnJournalposter(finnPensjonJournalposterRequest(saker, FERDIGSTILTE_JOURNALSTATUSER, brukerIdenter.getFoedselsnummer()), emptySet()).journalposter());
		}
		return mapOgFiltrerJournalposter(tema, brukerIdenter, pensjonsaker, tilgangJournalposter);
	}

	/*
	 * Henter og filtrerer journalposter etter tilgangsreglene i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
	 */
	private Journalpostdata mapOgFiltrerJournalposter(List<String> tema,
													  BrukerIdenter brukerIdenter,
													  Map<Long, Pensjonsak> pensjonsaker,
													  List<ArkivJournalpost> tilgangJournalposter) {
		List<Journalpost> filtrerteJournalposter = tilgangJournalposter
				.stream()
				.map(journalpost -> {
					try {
						return arkivJournalpostMapper.map(journalpost, brukerIdenter,
								Optional.ofNullable(journalpost.saksrelasjon()).map(ArkivSaksrelasjon::sakId).map(pensjonsaker::get));
					} catch (IllegalArgumentException e) {
						log.warn("Klarte ikke å mappe arkivJournalpost med id={} til tilgangsjournalpost. Tilgang blir avvist. Feilmelding={}", journalpost.journalpostId(), e.getMessage(), e);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.filter(journalpost -> utledTilgangService.utledTilgangJournalpost(journalpost.getTilgang(), brukerIdenter.getIdenter()).isEmpty())
				// Filtrer ut midlertidige journalposter som ikke har riktig tema.
				.filter(journalpost -> tema.contains(journalpost.getTema()))
				.toList();
		return new Journalpostdata(tilgangJournalposter.size(), filtrerteJournalposter);
	}

	/*
	 * 1c) Bruker får kun se midlertidige og ferdigstilte journalposter.
	 */
	private FinnJournalposterRequest finnAlleJournalposterRequest(BrukerIdenter brukerIdenter, Saker saker) {
		return baseFinnJournalposterRequest(saker, MIDLERTIDIGE_OG_FERDIGSTILTE_JOURNALSTATUSER, brukerIdenter.getFoedselsnummer());
	}

	/*
	 * Modifikasjon av 1c - midlertidige journalposter vises ikke da de er uten sakstilknytning.
	 */
	private FinnJournalposterRequest finnFerdigstilteJournalposterRequest(Saker saker) {
		return baseFinnJournalposterRequest(saker, FERDIGSTILTE_JOURNALSTATUSER, emptyList());
	}

	/*
	 * 1d) Bruker får ikke se feilregistrerte journalposter.
	 */
	private FinnJournalposterRequest baseFinnJournalposterRequest(Saker saker, List<JournalStatusCode> inkluderJournalstatuser, List<String> foedselsnummer) {
		return FinnJournalposterRequest.builder()
				.gsakSakIds(saker.arkivsaker().stream().map(Joarksak::getId).toList())
				.fraDato(UtledTilgangService.TIDLIGST_INNSYN_DATO.format(DateTimeFormatter.ISO_LOCAL_DATE))
				.visFeilregistrerte(false)
				.alleIdenter(foedselsnummer)
				.journalstatuser(inkluderJournalstatuser)
				.journalposttyper(ALLE_JOURNALPOSTTYPER)
				.antallRader(9999)
				.build();
	}

	private FinnJournalposterRequest finnPensjonJournalposterRequest(Saker saker, List<JournalStatusCode> inkluderJournalstatuser, List<String> foedselsnummer) {
		return FinnJournalposterRequest.builder()
				.psakSakIds(saker.pensjonsaker().stream().map(Pensjonsak::sakId).toList())
				.fraDato(LocalDate.of(1900, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE))
				.visFeilregistrerte(false)
				.alleIdenter(foedselsnummer)
				.journalstatuser(inkluderJournalstatuser)
				.journalposttyper(ALLE_JOURNALPOSTTYPER)
				.antallRader(9999)
				.build();
	}
}
