package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.N;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.FEIL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.POL;


/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
@Component
public class UtledTilgangJournalpostService {

	private static final EnumSet<JournalStatusCode> JOURNALSTATUS_FERDIGSTILT = EnumSet.of(FL, FS, J, E);
	private static final EnumSet<JournalStatusCode> JOURNALSTATUS_MIDLERTIDIG = EnumSet.of(M, MO);
	private static final EnumSet<SkjermingTypeCode> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);

	public List<JournalpostDto> utledTilgangJournalpost(List<JournalpostDto> journalpostDtoList, BrukerIdenter identer) {

		return journalpostDtoList.stream()
				.filter(journalpostDto -> isBrukerPart(journalpostDto, identer))
				.filter(this::isNotJournalpostGDPRRestricted)
				.filter(this::isJournalpostFerdigstiltOrMidlertidig)
				.filter(this::isNotJournalpostKontrollsak)
				.filter(this::isJournalpostForvaltningsnotat)
				.filter(this::isNotJournalpostOrganInternt)
				.collect(Collectors.toList());
	}

	/**
	 * 1a) Bruker må være part for å se journalposter
	 */
	private boolean isBrukerPart(JournalpostDto journalpostDto, BrukerIdenter identer) {

		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (JOURNALSTATUS_MIDLERTIDIG.contains(journalStatusCode)) {
			return identer.getIdenter().contains(journalpostDto.getBruker().getBrukerId());
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalStatusCode)) {
			if (journalpostDto.getSaksrelasjon().getFagsystem() == FS22) {
				return identer.getIdenter().contains(journalpostDto.getSaksrelasjon().getAktoerId());
			} else if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.PEN) {
				return identer.getFoedselsnummer().contains(journalpostDto.getBruker().getBrukerId());
			}
		}
		return false;
	}

	/**
	 * 1c) Bruker får kun se ferdigstilte journalposter
	 */
	private boolean isJournalpostFerdigstiltOrMidlertidig(JournalpostDto journalpostDto) {
		return JOURNALSTATUS_FERDIGSTILT.contains(journalpostDto.getJournalstatus()) || JOURNALSTATUS_MIDLERTIDIG.contains(journalpostDto.getJournalstatus());
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private boolean isNotJournalpostKontrollsak(JournalpostDto journalpostDto) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (JOURNALSTATUS_MIDLERTIDIG.contains(journalStatusCode)) {
			return journalpostDto.getFagomrade() != FagomradeCode.KTR;
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalStatusCode)) {
			return !journalpostDto.getSaksrelasjon().getTema().equals(Tema.KTR.toString());
		}
		return true;
	}

	/**
	 * 1f) Bruker kan ikke få se journalposter som er begrenset ihht. gdpr
	 */
	private boolean isNotJournalpostGDPRRestricted(JournalpostDto journalpostDto) {
		return !GDPR_SKJERMING_TYPE.contains(journalpostDto.getSkjerming());
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" for å vise journalposten.
	 */
	private boolean isJournalpostForvaltningsnotat(JournalpostDto journalpostDto) {
		if (journalpostDto.getJournalposttype() == N) {
			return FORVALTNINGSNOTAT.equals(journalpostDto.getDokumenter().get(0).getKategori());
		}
		return true;
	}

	/**
	 * 1h) Journalposter som har organinterne dokumenter skal ikke vises
	 */
	private boolean isNotJournalpostOrganInternt(JournalpostDto journalpostDto) {
		return journalpostDto.getDokumenter().stream().noneMatch(dokumentInfoDto -> dokumentInfoDto.getOrganInternt() != null && dokumentInfoDto.getOrganInternt());
	}
}
