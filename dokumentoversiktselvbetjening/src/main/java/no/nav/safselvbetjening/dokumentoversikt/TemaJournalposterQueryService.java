package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SaksrelasjonDto;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.Saker;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.STATUS_OK;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class TemaJournalposterQueryService {

	private final JournalpostMapper journalpostMapper;
	private final UtledTilgangService utledTilgangService;

	public TemaJournalposterQueryService(JournalpostMapper journalpostMapper,
										 UtledTilgangService utledTilgangService) {
		this.journalpostMapper = journalpostMapper;
		this.utledTilgangService = utledTilgangService;
	}

	List<Sakstema> query(final Basedata basedata, List<JournalpostDto> journalpostDtos, final List<String> tema) {
		log.info("dokumentoversiktSelvbetjening henter tema/journalposter.");
		final BrukerIdenter brukerIdenter = basedata.getBrukerIdenter();
		final Saker saker = basedata.getSaker();
		Map<FagomradeCode, List<JournalpostDto>> temaMap = groupedByFagomrade(journalpostDtos, saker);

		List<Sakstema> sakstema = temaMap.entrySet().stream()
				// Filtrer ut midlertidige journalposter som ikke har riktig tema.
				.filter(entry -> tema.contains(entry.getKey().name()))
				.map(st -> mapSakstema(st, saker, brukerIdenter))
				.sorted(Comparator.comparing(Sakstema::getKode))
				.collect(Collectors.toList());
		log.info("dokumentoversiktSelvbetjening hentet tema/journalposter. antall_tema={}, antall_journalposter={}", sakstema.size(), journalpostDtos.size());
		return sakstema;
	}

	private Map<FagomradeCode, List<JournalpostDto>> groupedByFagomrade(List<JournalpostDto> filtrerteJournalposter, Saker saker) {
		Map<String, String> sakIdTemaMap = saker.getArkivsakIdTemaMap();
		Map<FagomradeCode, List<JournalpostDto>> temaMap = new HashMap<>();
		for (JournalpostDto journalpostDto : filtrerteJournalposter) {
			SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
			if (saksrelasjon != null && isNotBlank(saksrelasjon.getSakId())) {
				if (sakIdTemaMap.containsKey(saksrelasjon.getSakId())) {
					temaMap.computeIfAbsent(FagomradeCode.valueOf(sakIdTemaMap.get(saksrelasjon.getSakId())), k -> new ArrayList<>()).add(journalpostDto);
				} else {
					temaMap.computeIfAbsent(journalpostDto.getFagomrade(), k -> new ArrayList<>()).add(journalpostDto);
				}
			} else {
				temaMap.computeIfAbsent(journalpostDto.getFagomrade(), k -> new ArrayList<>()).add(journalpostDto);
			}
		}
		return temaMap;
	}

	private Sakstema mapSakstema(Map.Entry<FagomradeCode, List<JournalpostDto>> fagomradeCodeListEntry, Saker saker, BrukerIdenter brukerIdenter) {
		final Tema tema = FagomradeCode.toTema(fagomradeCodeListEntry.getKey());

		return Sakstema.builder()
				.kode(tema.name())
				.navn(tema.getTemanavn())
				.journalposter(fagomradeCodeListEntry.getValue().stream()
						.filter(Objects::nonNull)
						.map(jp -> journalpostMapper.map(jp, saker, brukerIdenter))
						.filter(journalpost -> utledTilgangService.utledTilgangJournalpost(journalpost, brukerIdenter))
						.map(journalpost -> setDokumentVariant(journalpost, brukerIdenter))
						.collect(Collectors.toList()))
				.build();
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
