package no.nav.safselvbetjening.journalpost;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.graphql.GraphQLException;
import no.nav.safselvbetjening.graphql.GraphQLRequestContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static no.nav.safselvbetjening.MDCUtils.MDC_CALL_ID;
import static no.nav.safselvbetjening.MDCUtils.MDC_CONSUMER_ID;
import static no.nav.safselvbetjening.MDCUtils.getConsumerIdFromToken;
import static no.nav.safselvbetjening.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_JOURNALPOSTID_ER_BLANK;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_JOURNALPOSTID_ER_IKKE_NUMERISK;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_KUNNE_IKKE_HENTE_INTERN_REQUESTCONTEXT;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_MIDLERTIDIG_TEKNISK_FEIL;
import static no.nav.safselvbetjening.graphql.ErrorCode.SERVER_ERROR;
import static no.nav.safselvbetjening.graphql.GraphQLRequestContext.KEY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Slf4j
@Component
public class JournalpostByIdDataFetcher implements DataFetcher<DataFetcherResult<Journalpost>> {

	private final JournalpostService journalpostService;

	public JournalpostByIdDataFetcher(JournalpostService journalpostService) {
		this.journalpostService = journalpostService;
	}

	@Override
	public DataFetcherResult<Journalpost> get(DataFetchingEnvironment environment) {
		try {
			final GraphQLRequestContext graphQLRequestContext = environment.getGraphQlContext().<GraphQLRequestContext>getOrEmpty(KEY)
					.orElseThrow(() -> GraphQLException.of(SERVER_ERROR, environment, FEILMELDING_KUNNE_IKKE_HENTE_INTERN_REQUESTCONTEXT));
			MDC.put(MDC_CALL_ID, graphQLRequestContext.getNavCallId());
			MDC.put(MDC_CONSUMER_ID, getConsumerIdFromToken(graphQLRequestContext.getTokenValidationContext()));

			final String journalpostId = environment.getArgument("journalpostId");
			validerJournalpostId(journalpostId, environment);

			Journalpost journalpost = journalpostService.queryJournalpost(journalpostId, environment);
			return DataFetcherResult.<Journalpost>newResult().data(journalpost).build();
		} catch (GraphQLException e) {
			log.warn("journalpostById feilet: " + e.getError().getMessage());
			return e.toDataFetcherResult();
		} catch (CallNotPermittedException e) {
			log.error("journalpostById circuitbreaker={} er åpen.", e.getCausingCircuitBreakerName(), e);
			return DataFetcherResult.<Journalpost>newResult()
					.error(SERVER_ERROR.construct(environment, "Circuitbreaker=" + e.getCausingCircuitBreakerName() + " er åpen." +
															   " Kall til denne tjenesten går ikke gjennom."))
					.build();
		} catch (Exception e) {
			log.error("journalpostById midlertidig teknisk feil. " +
					  "Dette er som oftest forårsaket av midlertidige feil på nettverk/kobling mellom apper. Se stacktrace. message={}",
					e.getMessage(), e);
			return DataFetcherResult.<Journalpost>newResult()
					.error(SERVER_ERROR.construct(environment, FEILMELDING_MIDLERTIDIG_TEKNISK_FEIL))
					.build();
		} finally {
			MDC.clear();
		}
	}

	private void validerJournalpostId(String journalpostId, DataFetchingEnvironment environment) {
		if (isBlank(journalpostId)) {
			throw GraphQLException.of(BAD_REQUEST, environment, FEILMELDING_JOURNALPOSTID_ER_BLANK);
		}

		if (!isNumeric(journalpostId)) {
			throw GraphQLException.of(BAD_REQUEST, environment, FEILMELDING_JOURNALPOSTID_ER_IKKE_NUMERISK.formatted(journalpostId));
		}
	}
}
