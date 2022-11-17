package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Journalpost;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
class JournalposterQueryService {

	List<Journalpost> query(final Journalpostdata journalpostdata) {
		log.info("dokumentoversiktSelvbetjening henter /journalposter.");
		List<Journalpost> journalposts = journalpostdata.getJournalposter().stream()
				.sorted(Comparator.comparing(Journalpost::getJournalpostId).reversed())
				.collect(Collectors.toList());
		log.info("dokumentoversiktSelvbetjening hentet /journalposter. antall_journalposter={}/{}",
				journalpostdata.getAntallEtterFiltrering(), journalpostdata.getAntallFoerFiltrering());
		return journalposts;
	}
}
