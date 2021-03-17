package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.N;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_IM;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_NETS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.FEIL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.POL;


/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
@Slf4j
@Component
public class UtledTilgangHentDokument {

	private static final EnumSet<JournalStatusCode> JOURNALSTATUS_FERDIGSTILT = EnumSet.of(FL, FS, J, E);
	private static final EnumSet<JournalStatusCode> JOURNALSTATUS_MIDLERTIDIG = EnumSet.of(M, MO);
	private static final EnumSet<SkjermingTypeCode> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);
	private static final EnumSet<MottaksKanalCode> ACCEPTED_MOTTAKS_KANAL = EnumSet.of(SKAN_IM, SKAN_NETS, SKAN_PEN);

	public boolean utledTilgangJournalpost(TilgangJournalpostDto tilgangJournalpostDto, BrukerIdenter brukerIdenter) {
		return isBrukerPart(tilgangJournalpostDto, brukerIdenter) &&
				isJournalpostFerdigstiltOrMidlertidig(tilgangJournalpostDto);
	}

	/**
	 * 1a) Bruker må være part for å se journalposter
	 */
	private boolean isBrukerPart(TilgangJournalpostDto tilgangJournalpostDto, BrukerIdenter identer) {

		JournalStatusCode journalStatusCode = tilgangJournalpostDto.getJournalStatus();

		if (JOURNALSTATUS_MIDLERTIDIG.contains(journalStatusCode)) {
			return identer.getIdenter().contains(tilgangJournalpostDto.getBruker().getBrukerId());
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalStatusCode)) {
			if (FS22.toString().equals(tilgangJournalpostDto.getSak().getFagsystem())) {
				return identer.getIdenter().contains(tilgangJournalpostDto.getSak().getAktoerId());
			} else if (PEN.toString().equals(tilgangJournalpostDto.getSak().getFagsystem())) {
				return identer.getFoedselsnummer().contains(tilgangJournalpostDto.getBruker().getBrukerId());
			}
		}
		return false;
	}

	/**
	 * 1c) Bruker får kun se ferdigstilte journalposter
	 */
	private boolean isJournalpostFerdigstiltOrMidlertidig(TilgangJournalpostDto tilgangJournalpostDto) {
		return JOURNALSTATUS_FERDIGSTILT.contains(tilgangJournalpostDto.getJournalStatus()) || JOURNALSTATUS_MIDLERTIDIG.contains(tilgangJournalpostDto.getJournalStatus());
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private boolean isNotJournalpostKontrollsak(TilgangJournalpostDto tilgangJournalpostDto) {
		JournalStatusCode journalStatusCode = tilgangJournalpostDto.getJournalStatus();

		if (JOURNALSTATUS_MIDLERTIDIG.contains(journalStatusCode)) {
			return tilgangJournalpostDto.getFagomrade() != FagomradeCode.KTR;
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalStatusCode)) {
			return !tilgangJournalpostDto.getSak().getTema().equals(Tema.KTR.toString());
		} else {
			log.warn("Journalpost med journalpostId={} har status={} og tilgang blir derfor avvist. Journalposter må ha status midlertidig eller ferdigstilt for at bruker skal få tilgang.", tilgangJournalpostDto.getJournalpostId(), tilgangJournalpostDto.getJournalStatus());
			return false;
		}
	}

	/**
	 * 1f) Bruker kan ikke få se journalposter som er begrenset ihht. gdpr
	 */
	private boolean isNotJournalpostGDPRRestricted(TilgangJournalpostDto tilgangJournalpostDto) {
		return !GDPR_SKJERMING_TYPE.contains(tilgangJournalpostDto.getSkjerming());
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" for å vise journalposten.
	 */
	private boolean isJournalpostForvaltningsnotat(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getJournalpostType() == N) {
			//return FORVALTNINGSNOTAT.equals(tilgangJournalpostDto.getDokument());
			//Todo: Kategori mangler. Hente fra dokarkiv?
		}
		return true;
	}

	/**
	 * 1h) Journalposter som har organinterne dokumenter skal ikke vises
	 */
	private boolean isNotJournalpostOrganInternt(TilgangJournalpostDto tilgangJournalpostDto) {
		//return tilgangJournalpostDto.getDokument();
		//todo: Organinternt mangler.
		return true;
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	private boolean isAvsenderMottakerPart(TilgangJournalpostDto tilgangJournalpostDto, List<String> idents) {
		if (tilgangJournalpostDto.getJournalpostType() != JournalpostTypeCode.N) {
			return idents.contains(tilgangJournalpostDto.getAvsenderMottakerId());
		}
		return true;
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter
	 */
	private boolean isSkannetDokument(TilgangJournalpostDto tilgangJournalpostDto) {
		return ACCEPTED_MOTTAKS_KANAL.contains(tilgangJournalpostDto.getMottakskanal());
	}

	/**
	 * 2d) Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	 */
	private boolean isDokumentInnskrenketPartsinnsyn(TilgangJournalpostDto tilgangJournalpostDto) {
		//return (tilgangJournalpostDto.getDokument().getInnskrPartsinnsyn() != null && dokumentInfoDto.getInnskrPartsinnsyn()) ||
		//		(dokumentInfoDto.getInnskrTredjepart() != null && dokumentInfoDto.getInnskrTredjepart());
		//todo: Innskrenketpartsinnsyn og innskrenketTredjepart mangler
		return true;
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. gdpr skal ikke vises
	 */
	private boolean isDokumentGDPRRestricted(TilgangJournalpostDto tilgangJournalpostDto) {
		return GDPR_SKJERMING_TYPE.contains(tilgangJournalpostDto.getDokument().getVariant().getSkjerming());
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	private boolean isDokumentKassert(TilgangJournalpostDto tilgangJournalpostDto) {
		//return tilgangJournalpostDto.getKassert() != null && tilgangJournalpostDto.getKassert();
		//todo: kassert mangler;
		return true;
	}
}
