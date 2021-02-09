package no.nav.safselvbetjening.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class GraphQLWiring {
    private static final List<String> ALLE_TEMA = Arrays.asList("AAP", "AAR", "AGR", "BAR", "BID", "BIL", "DAG", "ENF",
            "ERS", "FAR", "FEI", "FOR", "FOS", "FRI", "FUL", "GEN", "GRA", "GRU", "HEL", "HJE", "IAR", "IND", "KON",
            "KTR", "MED", "MOB", "OMS", "OPA", "OPP", "PEN", "PER", "REH", "REK", "RPO", "RVE", "SAA", "SAK", "SAP",
            "SER", "SIK", "STO", "SUP", "SYK", "SYM", "TIL", "TRK", "TRY", "TSO", "TSR", "UFM", "UFO", "UKJ", "VEN",
            "YRA", "YRK");
    private final DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService;

    public GraphQLWiring(DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService) {
        this.dokumentoversiktSelvbetjeningService = dokumentoversiktSelvbetjeningService;
    }

    public RuntimeWiring createRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(DateTimeScalar.DATE_TIME)
                .type("Query", typeWiring -> typeWiring.dataFetcher("dokumentoversiktSelvbetjening", environment -> {
                    return dokumentoversiktSelvbetjeningService.queryDokumentoversikt(
                            environment.getArgumentOrDefault("ident", null),
                            temaArgument(environment));
                }))
                .build();
    }

    private List<String> temaArgument(DataFetchingEnvironment environment) {
        final List<String> tema = environment.getArgumentOrDefault("tema", new ArrayList<>());
        return tema.isEmpty() ? ALLE_TEMA : tema;
    }
}
