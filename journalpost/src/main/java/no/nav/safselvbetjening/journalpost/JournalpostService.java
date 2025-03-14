package no.nav.safselvbetjening.journalpost;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.dokarkiv.DokarkivConsumer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostMapper;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakRestConsumer;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.graphql.GraphQLRequestContext;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.TilgangsvalideringService;
import no.nav.safselvbetjening.tilgang.FullmaktInvalidException;
import no.nav.safselvbetjening.tilgang.NoValidTokensException;
import no.nav.safselvbetjening.tilgang.UserNotMatchingTokenException;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_INGEN_GYLDIG_TOKEN;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_INGEN_TILGANG_TIL_JOURNALPOST;
import static no.nav.safselvbetjening.graphql.ErrorCode.FORBIDDEN;
import static no.nav.safselvbetjening.graphql.ErrorCode.SERVER_ERROR;

@Slf4j
@Component
public class JournalpostService {
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");

	private final DokarkivConsumer dokarkivConsumer;
	private final IdentService identService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final ArkivJournalpostMapper arkivJournalpostMapper;
	private final UtledTilgangService utledTilgangService;
	private final TilgangsvalideringService tilgangsvalideringService;

	public JournalpostService(DokarkivConsumer dokarkivConsumer,
							  IdentService identService,
							  PensjonSakRestConsumer pensjonSakRestConsumer,
							  ArkivJournalpostMapper arkivJournalpostMapper,
							  UtledTilgangService utledTilgangService,
							  TilgangsvalideringService tilgangsvalideringService) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.identService = identService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.arkivJournalpostMapper = arkivJournalpostMapper;
		this.utledTilgangService = utledTilgangService;
		this.tilgangsvalideringService = tilgangsvalideringService;
	}

	Journalpost queryJournalpost(final long journalpostId, final DataFetchingEnvironment environment, final GraphQLRequestContext graphQLRequestContext) {
		ArkivJournalpost arkivJournalpost = dokarkivConsumer.journalpost(journalpostId, Set.of());
		validerRiktigJournalpost(journalpostId, arkivJournalpost, environment);
		BrukerIdenter brukerIdenter = identService.hentIdenter(arkivJournalpost);
		if (brukerIdenter.isEmpty()) {
			throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
		}

		try {
			Optional<Fullmakt> fullmaktOptional = tilgangsvalideringService.validerInnloggetBrukerOgFinnFullmakt(brukerIdenter,
					graphQLRequestContext.getTokenValidationContext());
			Optional<Pensjonsak> pensjonsakOpt = hentPensjonssak(brukerIdenter.getAktivFolkeregisterident(), arkivJournalpost);
			Journalpost journalpost = arkivJournalpostMapper.map(arkivJournalpost, brukerIdenter, pensjonsakOpt);
			String gjeldendeTema = journalpost.getTilgang().getGjeldendeTema();
			fullmaktOptional.ifPresent(fullmakt ->
					TilgangsvalideringService.validerFullmaktForTema(fullmakt, gjeldendeTema,
							fullmaktPresentAndValidAuditLog(journalpostId, gjeldendeTema)
					));

			var denyReasons = utledTilgangService.utledTilgangJournalpost(journalpost.getTilgang(), brukerIdenter.getIdenter());
			if (!denyReasons.isEmpty()) {
				throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_INGEN_TILGANG_TIL_JOURNALPOST);
			}

			return journalpost;
		} catch (IllegalArgumentException e) {
			log.warn("Klarte ikke å mappe arkivJournalpost med id={} til tilgangsjournalpost. Tilgang blir avvist. Feilmelding={}", arkivJournalpost.journalpostId(), e.getMessage(), e);
			throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_INGEN_TILGANG_TIL_JOURNALPOST);
		} catch (NoValidTokensException e) {
			throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_INGEN_GYLDIG_TOKEN);
		} catch (UserNotMatchingTokenException e) {
			secureLog.warn("journalpostById(journalpostId={}) Innlogget bruker med ident={} matcher ikke bruker på journalpost og har ingen fullmakt. brukerIdenter={}",
					journalpostId, e.getIdent(), e.getIdenter());
			throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
		} catch (FullmaktInvalidException e) {
			secureLog.warn("journalpostById(journalpostId={}, tema={}) Innlogget bruker med ident={} har fullmakt som ikke dekker tema for dokument tilhørende bruker={}. Tilgang er avvist",
					journalpostId, e.getGjeldendeTema(),
					e.getFullmakt().fullmektig(), e.getFullmakt().fullmaktsgiver());
			throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
		}
	}

	// Har behov for å kunne vise ekte tema
	private Optional<Pensjonsak> hentPensjonssak(String bruker, ArkivJournalpost arkivJournalpost) {
		if (arkivJournalpost.isTilknyttetSak() && arkivJournalpost.saksrelasjon().isPensjonsak()) {
			return pensjonSakRestConsumer.hentPensjonssaker(bruker)
					.stream().filter(p -> p.sakId().equals(arkivJournalpost.saksrelasjon().sakId()))
					.findFirst();
		}
		return Optional.empty();
	}

	private static void validerRiktigJournalpost(long journalpostId, ArkivJournalpost arkivJournalpost, DataFetchingEnvironment environment) {
		Long arkivJournalpostId = arkivJournalpost.journalpostId();
		if (journalpostId != arkivJournalpostId) {
			throw GraphQLException.of(SERVER_ERROR, environment, "journalpostId som er returnert fra fagarkivet matcher ikke journalpostId argument fra query. " +
					"journalpostById.journalpostId=" + journalpostId + ", arkivJournalpost.journalpostId=" + arkivJournalpostId);
		}
	}

	private static Consumer<Fullmakt> fullmaktPresentAndValidAuditLog(long journalpostId, String gjeldendeTema) {
		return fullmakt -> {
			secureLog.info("journalpostById(journalpostId={}, tema={}) Innlogget bruker med ident={} bruker fullmakt med tema={} for dokument tilhørende bruker={}",
					journalpostId, gjeldendeTema,
					fullmakt.fullmektig(), fullmakt.tema(), fullmakt.fullmaktsgiver());
		};
	}

}
