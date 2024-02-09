package no.nav.safselvbetjening.graphql;

import graphql.schema.idl.RuntimeWiring;
import no.nav.safselvbetjening.dokumentoversikt.DokumentoversiktSelvbetjeningDataFetcher;
import no.nav.safselvbetjening.journalpost.JournalpostByIdDataFetcher;
import org.springframework.stereotype.Component;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static no.nav.safselvbetjening.graphql.DateTimeScalar.DATE_TIME;

@Component
public class GraphQLWiring {

	private final DokumentoversiktSelvbetjeningDataFetcher dokumentoversiktSelvbetjeningDataFetcher;
	private final JournalpostByIdDataFetcher journalpostByIdDataFetcher;

	public GraphQLWiring(DokumentoversiktSelvbetjeningDataFetcher dokumentoversiktSelvbetjeningDataFetcher,
						 JournalpostByIdDataFetcher journalpostByIdDataFetcher) {
		this.dokumentoversiktSelvbetjeningDataFetcher = dokumentoversiktSelvbetjeningDataFetcher;
		this.journalpostByIdDataFetcher = journalpostByIdDataFetcher;
	}

	public RuntimeWiring createRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.scalar(DATE_TIME)
				.type(newTypeWiring("Query")
						.dataFetcher("dokumentoversiktSelvbetjening", dokumentoversiktSelvbetjeningDataFetcher)
						.dataFetcher("journalpostById", journalpostByIdDataFetcher))
				.build();
	}
}
