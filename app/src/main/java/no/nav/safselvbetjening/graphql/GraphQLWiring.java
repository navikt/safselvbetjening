package no.nav.safselvbetjening.graphql;

import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class GraphQLWiring {
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
                            environment.getArgumentOrDefault("tema", new ArrayList<>()));
                }))
                .build();
    }
}
