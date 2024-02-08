package no.nav.safselvbetjening.journalpost;

import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Journalpost;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JournalpostService {

	Journalpost queryJournalpost(final String journalpostId, final DataFetchingEnvironment environment) {
		return DummyJournalpost.stub(journalpostId);
	}
}
