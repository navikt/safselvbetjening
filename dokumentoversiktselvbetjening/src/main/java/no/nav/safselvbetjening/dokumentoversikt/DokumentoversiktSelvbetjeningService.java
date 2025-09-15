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
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalposter;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
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
	private static final String TIDLIGST_INNSYN_DATO_PEN_UFO = LocalDate.of(1900, 1, 1).toString();
	private static final String TIDLIGST_INNSYN_DATO_GENERELL = UtledTilgangService.TIDLIGST_INNSYN_DATO.format(ISO_LOCAL_DATE);

	private final IdentService identService;
	private final SakService sakService;
	private final DokarkivConsumer dokarkivConsumer;
	private final ArkivJournalpostMapper arkivJournalpostMapper;
	private final UtledTilgangService utledTilgangService;
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;

	public DokumentoversiktSelvbetjeningService(IdentService identService,
												SakService sakService,
												DokarkivConsumer dokarkivConsumer,
												ArkivJournalpostMapper arkivJournalpostMapper,
												UtledTilgangService utledTilgangService,
												SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.identService = identService;
		this.sakService = sakService;
		this.dokarkivConsumer = dokarkivConsumer;
		this.arkivJournalpostMapper = arkivJournalpostMapper;
		this.utledTilgangService = utledTilgangService;
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
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
		/*
		 * 1c) Bruker får kun se midlertidige og ferdigstilte journalposter.
		 */
		return queryFilterJournalposter(basedata, tema, pensjonsaker, MIDLERTIDIGE_OG_FERDIGSTILTE_JOURNALSTATUSER);
	}

	Journalpostdata queryFiltrerSakstilknyttedeJournalposter(Basedata basedata, List<String> tema, Map<Long, Pensjonsak> pensjonsaker) {
		/*
		 * Modifikasjon av 1c - midlertidige journalposter vises ikke da de er uten sakstilknytning.
		 */
		return queryFilterJournalposter(basedata, tema, pensjonsaker, FERDIGSTILTE_JOURNALSTATUSER);
	}

	private Journalpostdata queryFilterJournalposter(Basedata basedata, List<String> tema, Map<Long, Pensjonsak> pensjonsaker, List<JournalStatusCode> journalStatusCodeList) {
		final BrukerIdenter brukerIdenter = basedata.brukerIdenter();
		final Saker saker = basedata.saker();

		// Kaller alltid finnJournalposter selv om bruker ikke har arkivsaker
		// Dette i tilfelle bruker kun har innsendte søknader
		Mono<List<ArkivJournalpost>> arkivsakJournalposter =
				dokarkivConsumer.finnJournalposter(finnArkivsakJournalposterRequest(saker, journalStatusCodeList, brukerIdenter.getFoedselsnummer()), emptySet())
						.map(ArkivJournalposter::journalposter)
						.switchIfEmpty(Mono.just(List.of()));

		Mono<List<ArkivJournalpost>> psakJournalposter =
				Mono.just(saker.pensjonsaker().isEmpty())
						.flatMap(emptyPensjonsaker -> {
							if (emptyPensjonsaker) {
								return Mono.empty();
							}
							return dokarkivConsumer.finnJournalposter(finnPensjonJournalposterRequest(saker, journalStatusCodeList), emptySet());
						})
						.map(ArkivJournalposter::journalposter)
						.switchIfEmpty(Mono.just(List.of()));

		List<ArkivJournalpost> arkivJournalposter = Flux.merge(arkivsakJournalposter, psakJournalposter)
				.flatMapIterable(Function.identity())
				.collectList()
				.blockOptional().orElse(List.of());

		return mapOgFiltrerJournalposter(tema, brukerIdenter, pensjonsaker, arkivJournalposter);
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
	 * 1d) Bruker får ikke se feilregistrerte journalposter.
	 */
	private FinnJournalposterRequest finnArkivsakJournalposterRequest(Saker saker, List<JournalStatusCode> inkluderJournalstatuser, List<String> foedselsnummer) {
		return FinnJournalposterRequest.builder()
				.gsakSakIds(saker.arkivsaker().stream().map(Joarksak::getId).toList())
				.fraDato(TIDLIGST_INNSYN_DATO_GENERELL)
				.visFeilregistrerte(false)
				.alleIdenter(foedselsnummer)
				.journalstatuser(inkluderJournalstatuser)
				.journalposttyper(ALLE_JOURNALPOSTTYPER)
				.antallRader(9999)
				.build();
	}

	/*
	 * 1d) Bruker får ikke se feilregistrerte (alderspensjon/uføretrygd) journalposter.
	 */
	private FinnJournalposterRequest finnPensjonJournalposterRequest(Saker saker, List<JournalStatusCode> inkluderJournalstatuser) {
		return FinnJournalposterRequest.builder()
				.psakSakIds(saker.pensjonsaker().stream().map(Pensjonsak::sakId).toList())
				.fraDato(TIDLIGST_INNSYN_DATO_PEN_UFO)
				.visFeilregistrerte(false)
				.journalstatuser(inkluderJournalstatuser)
				.journalposttyper(ALLE_JOURNALPOSTTYPER)
				.antallRader(9999)
				.build();
	}
}
