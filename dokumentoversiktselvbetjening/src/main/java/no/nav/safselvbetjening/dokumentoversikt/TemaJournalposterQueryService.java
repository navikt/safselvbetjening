package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TemaJournalposterQueryService {

	List<Sakstema> query(final Journalpostdata journalpostdata) {
		log.info("dokumentoversiktSelvbetjening henter /tema/journalposter.");
		Map<String, List<Journalpost>> temaMap = groupedByFagomrade(journalpostdata.getJournalposter());

		List<Sakstema> sakstema = temaMap.entrySet().stream()
				.map(TemaJournalposterQueryService::mapSakstema)
				.sorted(Comparator.comparing(Sakstema::getKode))
				.toList();
		log.info("dokumentoversiktSelvbetjening hentet /tema/journalposter. antall_tema={}, antall_journalposter={}/{}", sakstema.size(),
				journalpostdata.getAntallEtterFiltrering(), journalpostdata.getAntallFoerFiltrering());
		return sakstema;
	}

	private Map<String, List<Journalpost>> groupedByFagomrade(List<Journalpost> journalposter) {
		Map<String, List<Journalpost>> temaMap = new HashMap<>();
		for (Journalpost journalpost : journalposter) {
			temaMap.computeIfAbsent(journalpost.getTema(), k -> new ArrayList<>()).add(journalpost);
		}
		return temaMap;
	}

	private static Sakstema mapSakstema(Map.Entry<String, List<Journalpost>> temaMap) {
		final Tema tema = Tema.valueOf(temaMap.getKey());

		return Sakstema.builder()
				.kode(tema.name())
				.navn(tema.getTemanavn())
				.journalposter(temaMap.getValue())
				.build();
	}
}
