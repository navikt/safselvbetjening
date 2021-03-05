package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;

@Component
public class UtledTilgangJournalpostService {

	public UtledTilgangJournalpostService() {
	}

	public List<JournalpostDto> utledTilgangJournalpost(List<JournalpostDto> journalpostDtoList, BrukerIdenter identer) {

		return journalpostDtoList.stream()
				.filter(journalpostDto -> isBrukerPart(journalpostDto, identer.getIdenter()))
				.filter(journalpostDto -> isJournalpostFerdigstiltOrMidlertidig(journalpostDto.getJournalstatus()))
				.filter(journalpostDto -> !isJournalpostKontrollsak(journalpostDto))
				.filter(journalpostDto -> !isJournalpostGDPRRestricted(journalpostDto.getSkjerming()))
				.filter(journalpostDto -> !isJournalpostForvantningsnotat(journalpostDto))
				.filter(journalpostDto -> !isJournalpostOrganInternt(journalpostDto))
				.collect(Collectors.toList());
	}

	private boolean isBrukerPart(JournalpostDto journalpostDto, List<String> aktoerIds) {

		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (journalStatusCode == M || journalStatusCode == MO) {
			return aktoerIds.contains(journalpostDto.getBruker().getBrukerId());
		} else if (journalStatusCode == FS || journalStatusCode == FL || journalStatusCode == J || journalStatusCode == E) {
			if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.FS22) {
				return aktoerIds.contains(journalpostDto.getSaksrelasjon().getAktoerId());
			} else if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.PEN) {
				//todo: Sjekk brukers identer mot PSAK-ident (hva er PSAK-ident?) Riktig?
				return aktoerIds.contains(journalpostDto.getBruker().getBrukerId());
			}
		}
		return false;
	}

	private boolean isJournalpostFerdigstiltOrMidlertidig(JournalStatusCode journalStatusCode) {
		return journalStatusCode == M || journalStatusCode == MO || journalStatusCode == J || journalStatusCode == FS
				|| journalStatusCode == FL || journalStatusCode == E;
	}

	private boolean isJournalpostKontrollsak(JournalpostDto journalpostDto) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (journalStatusCode == M || journalStatusCode == MO) {
			return journalpostDto.getFagomrade() == FagomradeCode.KTR;
		} else if (journalStatusCode == FL || journalStatusCode == J || journalStatusCode == FS || journalStatusCode == E) {
			return journalpostDto.getSaksrelasjon().getTema().equals(Tema.KTR.toString());
		}
		return false;
	}

	private boolean isJournalpostGDPRRestricted(SkjermingTypeCode skjermingTypeCode) {
		return skjermingTypeCode == SkjermingTypeCode.POL || skjermingTypeCode == SkjermingTypeCode.FEIL;
	}

	private boolean isJournalpostForvantningsnotat(JournalpostDto journalpostDto) {
		if (journalpostDto.getJournalposttype() == JournalpostTypeCode.N) {
			return FORVALTNINGSNOTAT.equals(journalpostDto.getDokumenter().get(0).getKategori());
			//sjekkes om noen av underliggende dokumenter har dokumentkategori "forvaltningsnotat"
			//Isåfall journalpost + dokumenter ikke vises
			//NB: Alle notater har kun ett dokument, så dette kan enten implementeres som en "sjekk alle underliggende dokumenter" eller "sjekk hoveddokument"-regel - gjør det enkleste.
		}
		return false;
	}

	private boolean isJournalpostOrganInternt(JournalpostDto journalpostDto) {
		return journalpostDto.getDokumenter().stream().anyMatch(DokumentInfoDto::getOrganInternt);
	}
}
