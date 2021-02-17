package no.nav.safselvbetjening.graphql;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningDataFetcher;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
@Slf4j
public class GraphQLWiring {
    private final DokumentoversiktSelvbetjeningDataFetcher dokumentoversiktSelvbetjeningDataFetcher;

    public GraphQLWiring(DokumentoversiktSelvbetjeningDataFetcher dokumentoversiktSelvbetjeningDataFetcher) {
        this.dokumentoversiktSelvbetjeningDataFetcher = dokumentoversiktSelvbetjeningDataFetcher;
    }

    public RuntimeWiring createRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(DateTimeScalar.DATE_TIME)
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("dokumentoversiktSelvbetjening", dokumentoversiktSelvbetjeningDataFetcher))
                .build();
    }
}
