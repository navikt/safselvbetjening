package no.nav.safselvbetjening.dokumentoversikt;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
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
	private static final List<String> ALLE_TEMA = Stream.of(Tema.values()).map(Tema::name).collect(Collectors.toList());

	private final DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService;

	public DokumentoversiktSelvbetjeningDataFetcher(DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService) {
		this.dokumentoversiktSelvbetjeningService = dokumentoversiktSelvbetjeningService;
	}

	@Override
	public Object get(DataFetchingEnvironment environment) throws Exception {
		try {
			final GraphQLRequestContext graphQLRequestContext = environment.getContext();
			MDC.put(MDC_CALL_ID, graphQLRequestContext.getNavCallId());
			final String ident = environment.getArgument("ident");
			validateIdent(ident, environment);
//			validateTokenIdent(ident, environment);
			final List<String> tema = temaArgument(environment);

			if (environment.getSelectionSet().contains("tema/journalposter")) {
				Dokumentoversikt dokumentoversikt = dokumentoversiktSelvbetjeningService.queryDokumentoversikt(ident, tema, environment);
				return DataFetcherResult.newResult()
						.data(dokumentoversikt)
						.build();
			}
			Dokumentoversikt dokumentoversikt = dokumentoversiktSelvbetjeningService.queryTema(ident, tema, environment);
			return DataFetcherResult.newResult()
					.data(dokumentoversikt)
					.build();
		} catch (GraphQLException e) {
			log.warn("dokumentoversiktSelvbetjening feilet: " + e.getError().getMessage());
			return e.toDataFetcherResult();
		} catch (Exception e) {
			log.error("dokumentoversiktSelvbetjening ukjent teknisk feil", e);
			return DataFetcherResult.newResult()
					.error(SERVER_ERROR.construct(environment, "Ukjent teknisk feil."))
					.build();
		} finally {
			MDC.clear();
		}
	}

	private void validateIdent(final String ident, DataFetchingEnvironment environment) {
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
		if(!jwtToken.getJwtTokenClaims().containsClaim("pid", ident)) {
			throw GraphQLException.of(ErrorCode.UNAUTHORIZED, environment, "Brukers ident i token matcher ikke ident i query.");
		}
	}


	private List<String> temaArgument(DataFetchingEnvironment environment) {
		final List<String> tema = environment.getArgumentOrDefault("tema", new ArrayList<>());
		return tema.isEmpty() ? ALLE_TEMA : tema;
	}
}
