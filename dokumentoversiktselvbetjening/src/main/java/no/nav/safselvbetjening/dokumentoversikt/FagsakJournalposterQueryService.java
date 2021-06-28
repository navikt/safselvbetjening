package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Sak;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class FagsakJournalposterQueryService {
	List<Fagsak> query(final Journalpostdata journalpostdata) {
		log.info("dokumentoversiktSelvbetjening henter /fagsak/journalposter.");
		Map<Fagsak, List<Journalpost>> fagsakListMap = groupedByFagsak(journalpostdata.getJournalposter());
		List<Fagsak> fagsaker = fagsakListMap.entrySet().stream()
				.map(fagsakListEntry -> fagsakListEntry.getKey().toBuilder()
						.journalposter(fagsakListEntry.getValue())
						.build())
				.sorted(Comparator.comparing(Fagsak::getFagsaksystem))
				.collect(toList());
		log.info("dokumentoversiktSelvbetjening hentet /fagsak/journalposter. antall_fagsaker={}, antall_journalposter={}/{}.",
				fagsaker.size(), journalpostdata.getAntallEtterFiltrering(), journalpostdata.getAntallFoerFiltrering());
		return fagsaker;
	}

	private Map<Fagsak, List<Journalpost>> groupedByFagsak(List<Journalpost> journalposter) {
		Map<Fagsak, List<Journalpost>> temaMap = new HashMap<>();
		for (Journalpost journalpost : journalposter) {
			Sak sak = journalpost.getSak();
			if (sak != null && sak.isFagsak()) {
				temaMap.computeIfAbsent(Fagsak.builder()
						.fagsakId(sak.getFagsakId())
						.fagsaksystem(sak.getFagsaksystem())
						.tema(journalpost.getTema())
						.build(), k -> new ArrayList<>()).add(journalpost);
			}
		}
		return temaMap;
	}
}
