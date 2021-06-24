package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.Saker;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.STATUS_OK;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class JournalposterQueryService {

	private final JournalpostMapper journalpostMapper;
	private final UtledTilgangService utledTilgangService;

	JournalposterQueryService(JournalpostMapper journalpostMapper,
							  UtledTilgangService utledTilgangService) {
		this.journalpostMapper = journalpostMapper;
		this.utledTilgangService = utledTilgangService;
	}

	List<Journalpost> query(final Basedata basedata, final List<JournalpostDto> journalpostDtos, List<String> tema) {
		log.info("dokumentoversiktSelvbetjening henter journalposter.");
		final BrukerIdenter brukerIdenter = basedata.getBrukerIdenter();
		final Saker saker = basedata.getSaker();
		List<Journalpost> journalposts = journalpostDtos.stream()
				.map(journalpostDto -> journalpostMapper.map(journalpostDto, saker, brukerIdenter))
				.filter(journalpost -> utledTilgangService.utledTilgangJournalpost(journalpost, brukerIdenter))
				.map(journalpost -> setDokumentVariant(journalpost, brukerIdenter))
				.filter(journalpost -> tema.contains(journalpost.getTema()))
				.sorted(Comparator.comparing(Journalpost::getJournalpostId).reversed())
				.collect(Collectors.toList());
		log.info("dokumentoversiktSelvbetjening henter journalposter. antall_journalposter={}", journalposts.size());
		return journalposts;
	}

	private Journalpost setDokumentVariant(Journalpost journalpost, BrukerIdenter brukerIdenter) {
		journalpost.getDokumenter().forEach(dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
				dokumentvariant -> {
					List<String> codes = utledTilgangService.utledTilgangDokument(journalpost, dokumentInfo, dokumentvariant, brukerIdenter);
					dokumentvariant.setBrukerHarTilgang(codes.isEmpty());
					dokumentvariant.setCode(codes.isEmpty() ? singletonList(STATUS_OK) : codes);
				}));
		return journalpost;
	}
}
