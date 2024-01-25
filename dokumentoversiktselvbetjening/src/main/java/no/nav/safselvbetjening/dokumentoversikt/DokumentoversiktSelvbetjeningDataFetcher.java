package no.nav.safselvbetjening.dokumentoversikt;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.dokumentoversikt.audit.DokumentoversiktAudit;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.fullmektig.FullmektigService;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.graphql.GraphQLRequestContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.CoreConfig.SYSTEM_CLOCK;
import static no.nav.safselvbetjening.MDCUtils.MDC_CALL_ID;
import static no.nav.safselvbetjening.MDCUtils.MDC_CONSUMER_ID;
import static no.nav.safselvbetjening.MDCUtils.MDC_FULLMAKT_TEMA;
import static no.nav.safselvbetjening.MDCUtils.getConsumerIdFromToken;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static no.nav.safselvbetjening.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_IDENT_ER_BLANK;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_IDENT_ER_UGYLDIG;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_KUNNE_IKKE_HENTE_INTERN_REQUESTCONTEXT;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_MIDLERTIDIG_TEKNISK_FEIL;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_TOKEN_MANGLER_I_HEADER;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_TOKEN_MISMATCH_INGEN_FULLMAKT;
import static no.nav.safselvbetjening.graphql.ErrorCode.SERVER_ERROR;
import static no.nav.safselvbetjening.graphql.ErrorCode.UNAUTHORIZED;
import static no.nav.safselvbetjening.graphql.GraphQLRequestContext.KEY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Slf4j
@Component
public class DokumentoversiktSelvbetjeningDataFetcher implements DataFetcher<Object> {

	private final DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService;
	private final TemaQueryService temaQueryService;
	private final TemaJournalposterQueryService temaJournalposterQueryService;
	private final FagsakQueryService fagsakQueryService;
	private final FagsakJournalposterQueryService fagsakJournalposterQueryService;
	private final JournalposterQueryService journalposterQueryService;
	private final FullmektigService fullmektigService;
	private final DokumentoversiktAudit audit;

	public DokumentoversiktSelvbetjeningDataFetcher(DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService,
													TemaQueryService temaQueryService,
													TemaJournalposterQueryService temaJournalposterQueryService,
													FagsakQueryService fagsakQueryService,
													FagsakJournalposterQueryService fagsakJournalposterQueryService,
													JournalposterQueryService journalposterQueryService,
													FullmektigService fullmektigService) {
		this.dokumentoversiktSelvbetjeningService = dokumentoversiktSelvbetjeningService;
		this.temaQueryService = temaQueryService;
		this.temaJournalposterQueryService = temaJournalposterQueryService;
		this.fagsakQueryService = fagsakQueryService;
		this.fagsakJournalposterQueryService = fagsakJournalposterQueryService;
		this.journalposterQueryService = journalposterQueryService;
		this.fullmektigService = fullmektigService;
		this.audit = new DokumentoversiktAudit(SYSTEM_CLOCK);
	}

	@Override
	public Object get(DataFetchingEnvironment environment) {
		try {
			final GraphQLRequestContext graphQLRequestContext = environment.getGraphQlContext().<GraphQLRequestContext>getOrEmpty(KEY)
					.orElseThrow(() -> GraphQLException.of(SERVER_ERROR, environment, FEILMELDING_KUNNE_IKKE_HENTE_INTERN_REQUESTCONTEXT));
			MDC.put(MDC_CALL_ID, graphQLRequestContext.getNavCallId());
			MDC.put(MDC_CONSUMER_ID, getConsumerIdFromToken(graphQLRequestContext.getTokenValidationContext()));
			final String identArgument = environment.getArgument("ident");
			validateIdentArgument(identArgument, environment);
			Optional<Fullmakt> fullmakt = validerInnloggetBrukerOgSjekkFullmakt(identArgument, environment, graphQLRequestContext);
			final List<String> tema = temaArgumentEllerFullmakt(environment, fullmakt);

			DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
			if (selectionSet.containsAnyOf("tema", "fagsak", "journalposter")) {
				Dokumentoversikt dokumentoversikt = fetchDokumentoversikt(identArgument, tema, environment);
				recordAuditLog(fullmakt, identArgument);
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
			log.error("dokumentoversiktSelvbetjening midlertidig teknisk feil. " +
					  "Dette er som oftest forårsaket av midlertidige feil på nettverk/kobling mellom apper. Se stacktrace. message={}",
					e.getMessage(), e);
			return DataFetcherResult.newResult()
					.error(SERVER_ERROR.construct(environment, FEILMELDING_MIDLERTIDIG_TEKNISK_FEIL))
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

	private void validateIdentArgument(String ident, DataFetchingEnvironment environment) {
		if (isBlank(ident)) {
			throw GraphQLException.of(BAD_REQUEST, environment, FEILMELDING_IDENT_ER_BLANK);
		}

		if (!isNumeric(ident)) {
			throw GraphQLException.of(BAD_REQUEST, environment, FEILMELDING_IDENT_ER_UGYLDIG);
		}
	}

	private Optional<Fullmakt> validerInnloggetBrukerOgSjekkFullmakt(String identArgument, DataFetchingEnvironment environment,
																	 GraphQLRequestContext graphQLRequestContext) {
		JwtToken subjectJwt = graphQLRequestContext.getTokenValidationContext().getFirstValidToken()
				.orElseThrow(() -> GraphQLException.of(UNAUTHORIZED, environment, FEILMELDING_TOKEN_MANGLER_I_HEADER));
		if (!subjectJwt.getJwtTokenClaims().containsClaim(CLAIM_PID, identArgument) &&
			!subjectJwt.getJwtTokenClaims().containsClaim(CLAIM_SUB, identArgument)) {
			Optional<Fullmakt> fullmakt = fullmektigService.finnFullmakt(subjectJwt, identArgument);
			if (fullmakt.isPresent()) {
				MDC.put(MDC_FULLMAKT_TEMA, fullmakt.get().tema().toString());
				return fullmakt;
			} else {
				throw GraphQLException.of(UNAUTHORIZED, environment, FEILMELDING_TOKEN_MISMATCH_INGEN_FULLMAKT);
			}
		}
		return Optional.empty();
	}

	static List<String> temaArgumentEllerFullmakt(DataFetchingEnvironment environment, Optional<Fullmakt> fullmakt) {
		final List<String> temaArgument = temaArgumentEllerDefault(environment);
		if (fullmakt.isPresent()) {
			List<String> fullmaktTema = fullmakt.get().tema();
			return temaArgument.stream()
					.filter(fullmaktTema::contains)
					.collect(Collectors.toList());
		}
		return temaArgument;
	}

	private static List<String> temaArgumentEllerDefault(DataFetchingEnvironment environment) {
		final List<String> temaArgument = environment.getArgumentOrDefault("tema", new ArrayList<>());
		return temaArgument.isEmpty() ? Tema.tillattInnsynNavNoString() : temaArgument;
	}

	private void recordAuditLog(Optional<Fullmakt> fullmaktOpt, String ident) {
		fullmaktOpt.ifPresentOrElse(audit::logSomFullmektig, () -> audit.logSomBruker(ident));
	}
}
