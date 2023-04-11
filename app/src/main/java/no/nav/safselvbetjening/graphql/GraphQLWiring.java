package no.nav.safselvbetjening.graphql;

import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningDataFetcher;
import org.springframework.stereotype.Component;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static no.nav.safselvbetjening.graphql.DateTimeScalar.DATE_TIME;

@Component
@Slf4j
public class GraphQLWiring {

	private final DokumentoversiktSelvbetjeningDataFetcher dokumentoversiktSelvbetjeningDataFetcher;

	public GraphQLWiring(DokumentoversiktSelvbetjeningDataFetcher dokumentoversiktSelvbetjeningDataFetcher) {
		this.dokumentoversiktSelvbetjeningDataFetcher = dokumentoversiktSelvbetjeningDataFetcher;
	}

	public RuntimeWiring createRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.scalar(DATE_TIME)
				.type(newTypeWiring("Query")
						.dataFetcher("dokumentoversiktSelvbetjening", dokumentoversiktSelvbetjeningDataFetcher))
				.build();
	}
}
