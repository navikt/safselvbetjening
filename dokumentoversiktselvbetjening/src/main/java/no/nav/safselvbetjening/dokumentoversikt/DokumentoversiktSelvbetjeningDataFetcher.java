package no.nav.safselvbetjening.dokumentoversikt;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.graphql.ErrorCode;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.graphql.GraphQLRequestContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.safselvbetjening.MDCUtils.MDC_CALL_ID;
import static no.nav.safselvbetjening.MDCUtils.MDC_CONSUMER_ID;
import static no.nav.safselvbetjening.MDCUtils.getConsumerIdFromToken;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static no.nav.safselvbetjening.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.safselvbetjening.graphql.ErrorCode.SERVER_ERROR;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class DokumentoversiktSelvbetjeningDataFetcher implements DataFetcher<Object> {
	private static final List<String> ALLE_TEMA_UTEN_KONTROLL = Stream.of(Tema.values())
			.filter(t -> t != Tema.KTR)
			.map(Tema::name)
			.collect(Collectors.toList());

	private final DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService;
	private final TemaQueryService temaQueryService;
	private final TemaJournalposterQueryService temaJournalposterQueryService;
	private final FagsakQueryService fagsakQueryService;
	private final FagsakJournalposterQueryService fagsakJournalposterQueryService;
	private final JournalposterQueryService journalposterQueryService;

	public DokumentoversiktSelvbetjeningDataFetcher(DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService,
													TemaQueryService temaQueryService,
													TemaJournalposterQueryService temaJournalposterQueryService,
													FagsakQueryService fagsakQueryService,
													FagsakJournalposterQueryService fagsakJournalposterQueryService,
													JournalposterQueryService journalposterQueryService) {
		this.dokumentoversiktSelvbetjeningService = dokumentoversiktSelvbetjeningService;
		this.temaQueryService = temaQueryService;
		this.temaJournalposterQueryService = temaJournalposterQueryService;
		this.fagsakQueryService = fagsakQueryService;
		this.fagsakJournalposterQueryService = fagsakJournalposterQueryService;
		this.journalposterQueryService = journalposterQueryService;
	}

	@Override
	public Object get(DataFetchingEnvironment environment) throws Exception {
		try {
			final GraphQLRequestContext graphQLRequestContext = environment.getContext();
			MDC.put(MDC_CALL_ID, graphQLRequestContext.getNavCallId());
			MDC.put(MDC_CONSUMER_ID, getConsumerIdFromToken(graphQLRequestContext.getTokenValidationContext()));
			final String ident = environment.getArgument("ident");
			validateIdent(ident, environment);
			validateTokenIdent(ident, environment);
			final List<String> tema = temaArgument(environment);

			DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
			if (selectionSet.containsAnyOf("tema", "fagsak", "journalposter")) {
				Dokumentoversikt dokumentoversikt = fetchDokumentoversikt(ident, tema, environment);
				return DataFetcherResult.newResult()
						.data(dokumentoversikt)
						.build();
			}
			return DataFetcherResult.newResult().data(Dokumentoversikt.empty()).build();
		} catch (GraphQLException e) {
			log.warn("dokumentoversiktSelvbetjening feilet: " + e.getError().getMessage());
			return e.toDataFetcherResult();
		} catch (CallNotPermittedException e) {
			log.error("dokumentoversiktSelvbetjening circuitbreaker={} er åpen.", e.getCausingCircuitBreakerName(), e);
			return DataFetcherResult.newResult()
					.error(SERVER_ERROR.construct(environment, "Circuitbreaker=" + e.getCausingCircuitBreakerName() + " er åpen." +
							" Kall til denne tjenesten går ikke gjennom."))
					.build();
		} catch (Exception e) {
			log.error("dokumentoversiktSelvbetjening ukjent teknisk feil", e);
			return DataFetcherResult.newResult()
					.error(SERVER_ERROR.construct(environment, "Ukjent teknisk feil."))
					.build();
		} finally {
			MDC.clear();
		}
	}

	Dokumentoversikt fetchDokumentoversikt(final String ident, final List<String> tema,
										   final DataFetchingEnvironment environment) {
		final DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
		final Basedata basedata = dokumentoversiktSelvbetjeningService.queryBasedata(ident, tema, environment);
		final Journalpostdata filtrerteJournalpostdata = filtrerteJournalposter(basedata, tema, selectionSet);
		final List<Sakstema> sakstema = fetchSakstema(basedata, filtrerteJournalpostdata, selectionSet);
		final List<Fagsak> fagsaker = fetchFagsak(basedata, filtrerteJournalpostdata, selectionSet);
		final List<Journalpost> journalposter = fetchJournalposter(filtrerteJournalpostdata, selectionSet);
		return Dokumentoversikt.builder()
				.tema(sakstema)
				.fagsak(fagsaker)
				.journalposter(journalposter)
				.build();
	}

	private Journalpostdata filtrerteJournalposter(Basedata basedata, List<String> tema,
												   DataFetchingFieldSelectionSet selectionSet) {
		if (selectionSet.containsAnyOf("tema/journalposter", "journalposter")) {
			return dokumentoversiktSelvbetjeningService.queryFiltrerAlleJournalposter(basedata, tema);
		} else if (selectionSet.containsAnyOf("fagsak/journalposter")) {
			return dokumentoversiktSelvbetjeningService.queryFiltrerSakstilknyttedeJournalposter(basedata, tema);
		}
		return Journalpostdata.empty();
	}

	private List<Sakstema> fetchSakstema(Basedata basedata, Journalpostdata journalpostdata,
										 DataFetchingFieldSelectionSet selectionSet) {
		if (selectionSet.contains("tema")) {
			if (selectionSet.contains("tema/journalposter")) {
				return temaJournalposterQueryService.query(journalpostdata);
			}
			return temaQueryService.query(basedata);
		} else {
			return new ArrayList<>();
		}
	}

	private List<Fagsak> fetchFagsak(Basedata basedata, Journalpostdata journalpostdata,
									 DataFetchingFieldSelectionSet selectionSet) {
		if (selectionSet.contains("fagsak")) {
			if (selectionSet.contains("fagsak/journalposter")) {
				return fagsakJournalposterQueryService.query(journalpostdata);
			}
			return fagsakQueryService.query(basedata);
		} else {
			return new ArrayList<>();
		}
	}

	private List<Journalpost> fetchJournalposter(Journalpostdata journalpostdata,
												 DataFetchingFieldSelectionSet selectionSet) {
		if (selectionSet.contains("journalposter")) {
			return journalposterQueryService.query(journalpostdata);
		} else {
			return new ArrayList<>();
		}
	}

	private void validateIdent(String ident, DataFetchingEnvironment environment) {
		if (isBlank(ident)) {
			throw GraphQLException.of(BAD_REQUEST, environment, "Ident argumentet er blankt.");
		}

		if (!isNumeric(ident)) {
			throw GraphQLException.of(BAD_REQUEST, environment, "Ident argumentet er ugyldig. " +
					"Det må være et fødselsnummer eller en aktørid.");
		}
	}

	private void validateTokenIdent(String ident, DataFetchingEnvironment environment) {
		final GraphQLRequestContext graphQLRequestContext = environment.getContext();
		JwtToken jwtToken = graphQLRequestContext.getTokenValidationContext().getFirstValidToken()
				.orElseThrow(() -> GraphQLException.of(ErrorCode.UNAUTHORIZED, environment, "Ingen gyldige tokens i Authorization headeren."));
		if (!jwtToken.getJwtTokenClaims().containsClaim(CLAIM_PID, ident) &&
				!jwtToken.getJwtTokenClaims().containsClaim(CLAIM_SUB, ident)) {
			throw GraphQLException.of(ErrorCode.UNAUTHORIZED, environment, "Brukers ident i token matcher ikke ident i query. Ident må ligge i pid eller sub claim.");
		}
	}

	private List<String> temaArgument(DataFetchingEnvironment environment) {
		final List<String> tema = environment.getArgumentOrDefault("tema", new ArrayList<>());
		return tema.isEmpty() ? ALLE_TEMA_UTEN_KONTROLL : tema;
	}
}
