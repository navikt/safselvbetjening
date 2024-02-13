package no.nav.safselvbetjening.graphql;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static graphql.ErrorType.DataFetchingException;
import static graphql.ErrorType.ExecutionAborted;
import static graphql.ErrorType.ValidationError;
import static java.util.Collections.singletonMap;

/**
 * Kopiert fra navikt/pdl
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED(ExecutionAborted, "unauthorized"),
    FORBIDDEN(ExecutionAborted, "forbidden"),
    NOT_FOUND(ExecutionAborted, "not_found"),
    BAD_REQUEST(ValidationError, "bad_request"),
    SERVER_ERROR(DataFetchingException, "server_error");

    public static final String FEILMELDING_BRUKER_KAN_IKKE_UTLEDES = "Tilgang til dokument avvist fordi bruker ikke kan utledes";
    public static final String FEILMELDING_TOKEN_MANGLER_I_HEADER = "Ingen gyldige tokens i Authorization-header.";
    public static final String FEILMELDING_TOKEN_MISMATCH_INGEN_FULLMAKT = "Innlogget brukers ident i token matcher ikke ident i query. Innlogget bruker har heller ingen fullmakt overfor ident i query.";
    public static final String FEILMELDING_BRUKER_IKKE_FUNNET_I_PDL = "Finner ingen identer på person i PDL.";
    public static final String FEILMELDING_IDENT_ER_BLANK = "Ident i query er blank. Denne må være satt.";
    public static final String FEILMELDING_IDENT_ER_UGYLDIG = "Ident i query er ugyldig. Det må være et gyldig fødselsnummer eller aktørid.";
    public static final String FEILMELDING_JOURNALPOSTID_ER_BLANK = "journalpostId i argument til journalpostById query er blank eller null. Argumentet er påkrevd.";
    public static final String FEILMELDING_JOURNALPOSTID_ER_IKKE_NUMERISK = "journalpostId i argument til journalpostById query må være numerisk. journalpostId=%s";
    public static final String FEILMELDING_KUNNE_IKKE_HENTE_INTERN_REQUESTCONTEXT = "Kunne ikke hente intern requestcontext.";
    public static final String FEILMELDING_MIDLERTIDIG_TEKNISK_FEIL = "Midlertidig teknisk feil. Som oftest løses dette ved at bruker forsøker på nytt. Hvis feilen vedvarer og skaper ulemper for bruker: gi beskjed på Slack-kanal #team_dokumentløsninger.";

    private final ErrorClassification type;
    private final String text;

    public GraphQLError construct(DataFetchingEnvironment env, String message) {
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(type)
                .extensions(singletonMap("code", text))
                .build();
    }
}
