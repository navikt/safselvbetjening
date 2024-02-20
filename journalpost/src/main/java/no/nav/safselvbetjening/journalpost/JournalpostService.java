package no.nav.safselvbetjening.journalpost;

import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.dokarkiv.DokarkivConsumer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakRestConsumer;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.graphql.GraphQLRequestContext;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.graphql.ErrorCode.FORBIDDEN;
import static no.nav.safselvbetjening.graphql.ErrorCode.SERVER_ERROR;
import static no.nav.safselvbetjening.graphql.ErrorCode.UNAUTHORIZED;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_INGEN_GYLDIG_TOKEN;

@Slf4j
@Component
public class JournalpostService {

	private final DokarkivConsumer dokarkivConsumer;
	private final IdentService identService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final ArkivJournalpostMapper arkivJournalpostMapper;
	private final UtledTilgangService utledTilgangService;

	public JournalpostService(DokarkivConsumer dokarkivConsumer,
							  IdentService identService,
							  PensjonSakRestConsumer pensjonSakRestConsumer,
							  ArkivJournalpostMapper arkivJournalpostMapper,
							  UtledTilgangService utledTilgangService) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.identService = identService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.arkivJournalpostMapper = arkivJournalpostMapper;
		this.utledTilgangService = utledTilgangService;
	}

	Journalpost queryJournalpost(final String journalpostId, final DataFetchingEnvironment environment, final GraphQLRequestContext graphQLRequestContext) {
		ArkivJournalpost arkivJournalpost = dokarkivConsumer.journalpost(journalpostId, Set.of());
		validerRiktigJournalpost(journalpostId, arkivJournalpost, environment);
		BrukerIdenter brukerIdenter = identService.hentIdenter(arkivJournalpost);
		if (brukerIdenter.isEmpty()) {
			throw GraphQLException.of(FORBIDDEN, environment, FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
		}
		validerInnloggetBruker(brukerIdenter, environment, graphQLRequestContext);
		Optional<Pensjonsak> pensjonsakOpt = hentPensjonssak(brukerIdenter.getAktivFolkeregisterident(), arkivJournalpost);

		Journalpost journalpost = arkivJournalpostMapper.map(arkivJournalpost, brukerIdenter, pensjonsakOpt);

		boolean tilgang = utledTilgangService.utledTilgangJournalpost(journalpost, brukerIdenter);
		if (!tilgang) {
			throw GraphQLException.of(FORBIDDEN, environment, "Bruker har ikke tilgang til journalpost");
		}

		return journalpost;
	}

	// Har behov for å kunne vise ekte tema
	private Optional<Pensjonsak> hentPensjonssak(String bruker, ArkivJournalpost arkivJournalpost) {
		if (arkivJournalpost.isTilknyttetSak() && arkivJournalpost.saksrelasjon().isPensjonsak()) {
			return pensjonSakRestConsumer.hentPensjonssaker(bruker)
					.stream().filter(p -> p.sakId().equals(arkivJournalpost.saksrelasjon().sakId().toString()))
					.findFirst();
		}
		return Optional.empty();
	}

	private void validerInnloggetBruker(
			BrukerIdenter brukerIdenter,
			DataFetchingEnvironment environment,
			GraphQLRequestContext graphQLRequestContext
	) {
		JwtToken subjectJwt = graphQLRequestContext.getTokenValidationContext().getFirstValidToken();
		if (subjectJwt == null) {
			throw GraphQLException.of(UNAUTHORIZED, environment, FEILMELDING_INGEN_GYLDIG_TOKEN);
		}
		List<String> identer = brukerIdenter.getIdenter();
		String pid = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_PID);
		String sub = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_SUB);
		if (!identer.contains(pid) && !identer.contains(sub)) {
			throw GraphQLException.of(UNAUTHORIZED, environment, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
		}
	}

	private static void validerRiktigJournalpost(String journalpostId, ArkivJournalpost arkivJournalpost, DataFetchingEnvironment environment) {
		Long arkivJournalpostId = arkivJournalpost.journalpostId();
		if (!journalpostId.equals(arkivJournalpostId.toString())) {
			throw GraphQLException.of(SERVER_ERROR, environment, "journalpostId som er returnert fra fagarkivet matcher ikke journalpostId argument fra query. " +
																 "journalpostById.journalpostId=" + journalpostId + ", arkivJournalpost.journalpostId=" + arkivJournalpostId);
		}
	}
}
