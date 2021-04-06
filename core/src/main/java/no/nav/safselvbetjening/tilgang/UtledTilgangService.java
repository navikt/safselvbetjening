package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangDokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangJournalpost;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
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
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.FEILREGISTRERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KASSERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KONTROLLSAK;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.ORGANINTERNT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.UGYLDIG_JOURNALSTATUS;

/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
@Slf4j
@Component
public class UtledTilgangService {

	private static final EnumSet<SkjermingTypeCode> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);
	private static final List<String> ACCEPTED_MOTTAKS_KANAL = List.of(SKAN_IM.toString(), SKAN_NETS.toString(), SKAN_PEN.toString());
	private static final EnumSet<JournalStatusCode> JOURNALSTATUS_FERDIGSTILT = EnumSet.of(FL, FS, J, E);
	private static final EnumSet<JournalStatusCode> JOURNALSTATUS_MIDLERTIDIG = EnumSet.of(M, MO);

	private final LocalDateTime tidligstInnsynDato;

	public UtledTilgangService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.tidligstInnsynDato = safSelvbetjeningProperties.getTidligstInnsynDato().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
	}

	public void utledTilgangHentDokument(UtledTilgangJournalpost utledTilgangJournalpost, BrukerIdenter brukerIdenter) {
		if(!isBrukerPart(utledTilgangJournalpost, brukerIdenter)){
			throw new HentTilgangDokumentException(PARTSINNSYN, "Tilgang til journalpost avvist fordi bruker ikke er part");
		}
		/* if(..) then throw exception
		verifyAccessInnsynsdatoJournalpost(tilgangJournalpostDto);
		verifyAccessFerdigstilteJournalposter(tilgangJournalpostDto);
		verifyAccessFeilregistrerteJournalposter(tilgangJournalpostDto);
		verifyAccessKontrollsakJournalpost(tilgangJournalpostDto);
		verifyAccessGDPRJournalpost(tilgangJournalpostDto);
		verifyAccessForvaltningsnotatJournalpost(tilgangJournalpostDto);
		verifyAccessOrganinternJournalpost(tilgangJournalpostDto);

		verifyAccessAndreParterDokument(tilgangJournalpostDto, brukerIdenter.getIdenter());
		verifyAccessSkannetDokument(tilgangJournalpostDto);
		verifyAccessInnskrenketPartsinnsynDokument(tilgangJournalpostDto.getDokument());
		verfyAccessGDPRDokument(tilgangJournalpostDto.getDokument());
		verifyAccessKassertDokument(tilgangJournalpostDto);

		 */
	}

	/**
	 * 1a) Bruker må være part for å se journalposter
	 */
	private boolean isBrukerPart(UtledTilgangJournalpost utledTilgangJournalpost, BrukerIdenter identer) {

		JournalStatusCode journalStatusCode = utledTilgangJournalpost.getJournalstatusCode();

		if (JournalStatusCode.getJournalstatusMidlertidig().contains(journalStatusCode)) {
			return identer.getIdenter().contains(utledTilgangJournalpost.getUtledTilgangBruker().getBrukerId());
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(journalStatusCode)) {
			if (FS22.toString().equals(utledTilgangJournalpost.getUtledTilgangSak().getFagsystem())) {
				return identer.getIdenter().contains(utledTilgangJournalpost.getUtledTilgangSak().getAktoerId());
			} else if (FagsystemCode.PEN.toString().equals(utledTilgangJournalpost.getUtledTilgangSak().getFagsystem())) {
				return identer.getFoedselsnummer().contains(utledTilgangJournalpost.getUtledTilgangBruker().getBrukerId());
			}
		}
		return false;
	}

	/**
	 * 1b) Bruker får ikke se journalposter som er journalført før 04.06.2016
	 */
	private boolean isJournalfoertDatoAfterInnsynsdato(JournalpostDto journalpostDto) {
		if (journalpostDto.getJournalDato() == null) {
			return true;
		} else {
			return journalpostDto.getJournalDato().after(tidligstInnsynDato);
		}
	}

	/**
	 * 1c) Bruker får kun se ferdigstilte journalposter
	 */
	private boolean isJournalpostFerdigstiltOrMidlertidig(UtledTilgangJournalpost utledTilgangJournalpost) {
		return JOURNALSTATUS_FERDIGSTILT.contains(utledTilgangJournalpost.getJournalstatusCode()) || JOURNALSTATUS_MIDLERTIDIG.contains(utledTilgangJournalpost.getJournalstatusCode());
	}


	/**
	 * 1b) Bruker får ikke se journalposter som er opprettet eller journalført før 04.06.2016 (dato or lansering av innsynsløsningen ble lansert)
	 */
	private void verifyAccessInnsynsdatoJournalpost(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tidligstInnsynDato.isAfter(tilgangJournalpostDto.getDatoOpprettet()) || (tilgangJournalpostDto.getJournalfoertDato() != null && tidligstInnsynDato.isAfter(tilgangJournalpostDto.getJournalfoertDato()))) {
			throw new HentTilgangDokumentException(INNSYNSDATO, "Tilgang til journalpost avvist fordi journalposten er opprettet før tidligst innsynsdato");
		}
	}

	/**
	 * 1c) Bruker får kun se ferdigstilte journalposter
	 */
	private void verifyAccessFerdigstilteJournalposter(TilgangJournalpostDto tilgangJournalpostDto) {
		if (!(JournalStatusCode.getJournalstatusFerdigstilt().contains(tilgangJournalpostDto.getJournalStatus()) ||
				JournalStatusCode.getJournalstatusMidlertidig().contains(tilgangJournalpostDto.getJournalStatus()))) {
			throw new HentTilgangDokumentException(UGYLDIG_JOURNALSTATUS, "Tilgang til journalpost avvist fordi journalpost er ikke ferdigstilt eller midlertidig");
		}
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private boolean isNotJournalpostKontrollsak(JournalpostDto journalpostDto) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (JOURNALSTATUS_MIDLERTIDIG.contains(journalStatusCode)) {
			return journalpostDto.getFagomrade() != FagomradeCode.KTR;
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalStatusCode)) {
			if (journalpostDto.getSaksrelasjon() != null) {
				return !Tema.KTR.toString().equals(journalpostDto.getSaksrelasjon().getTema());
			} else {
				return journalpostDto.getFagomrade() != FagomradeCode.KTR;
			}
		} else {
			log.warn("Journalpost med journalpostId={} har status={} og tilgang blir derfor avvist. Journalposter må ha status midlertidig eller ferdigstilt for at bruker skal få tilgang.", journalpostDto.getJournalpostId(), journalpostDto.getJournalstatus());
			return false;
		}
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


	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private boolean isNotJournalpostKontrollsak(JournalpostDto journalpostDto) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (JournalStatusCode.getJournalstatusMidlertidig().contains(journalStatusCode)) {
			return journalpostDto.getFagomrade() != FagomradeCode.KTR;
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(journalStatusCode)) {
			return !journalpostDto.getSaksrelasjon().getTema().equals(Tema.KTR.toString());
		}
		return false;
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

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	private boolean isAvsenderMottakerPart(JournalpostDto journalpostDto, List<String> idents) {
		if (journalpostDto.getJournalposttype() != JournalpostTypeCode.N) {
			return idents.contains(journalpostDto.getAvsenderMottakerId());
		}
		return true;
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter
	 */
	private boolean isSkannetDokument(JournalpostDto journalpostDto) {
		return ACCEPTED_MOTTAKS_KANAL.contains(journalpostDto.getMottakskanal());
	}

	/**
	 * 2d) Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	 */
	private boolean isDokumentInnskrenketPartsinnsyn(DokumentInfoDto dokumentInfoDto) {
		return (dokumentInfoDto.getInnskrPartsinnsyn() != null && dokumentInfoDto.getInnskrPartsinnsyn()) ||
				(dokumentInfoDto.getInnskrTredjepart() != null && dokumentInfoDto.getInnskrTredjepart());
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. gdpr skal ikke vises
	 */
	private boolean isDokumentGDPRRestricted(VariantDto variantDto) {
		return GDPR_SKJERMING_TYPE.contains(variantDto.getSkjerming());
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	private boolean isDokumentKassert(DokumentInfoDto dokumentInfoDto) {
		return dokumentInfoDto.getKassert() != null && dokumentInfoDto.getKassert();
	}

	/**
	 * 1d) Bruker får ikke se feilregistrerte journalposter
	 */
	private void verifyAccessFeilregistrerteJournalposter(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getFeilregistrert() != null && tilgangJournalpostDto.getFeilregistrert()) {
			throw new HentTilgangDokumentException(FEILREGISTRERT, "Tilgang til journalpost avvist fordi journalpost er feilregistrert");
		}
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private void verifyAccessKontrollsakJournalpost(TilgangJournalpostDto tilgangJournalpostDto) {
		JournalStatusCode journalStatusCode = tilgangJournalpostDto.getJournalStatus();

		if ((JournalStatusCode.getJournalstatusMidlertidig().contains(journalStatusCode) && tilgangJournalpostDto.getFagomrade() == FagomradeCode.KTR) ||
				(JournalStatusCode.getJournalstatusFerdigstilt().contains(journalStatusCode) && tilgangJournalpostDto.getSak().getTema().equals(Tema.KTR.toString()))) {
			throw new HentTilgangDokumentException(KONTROLLSAK, "Tilgang til journalpost avvist fordi journalpost er markert som kontrollsak");
		}
	}

	/**
	 * 1f) Bruker kan ikke få se journalposter som er begrenset ihht. gdpr
	 */
	private void verifyAccessGDPRJournalpost(TilgangJournalpostDto tilgangJournalpostDto) {
		if (GDPR_SKJERMING_TYPE.contains(tilgangJournalpostDto.getSkjerming())) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til journalpost avvist ihht. gdpr");
		}
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" for å vise journalposten.
	 */
	private void verifyAccessForvaltningsnotatJournalpost(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getJournalpostType() == N && !FORVALTNINGSNOTAT.equals(tilgangJournalpostDto.getDokument().getKategori())) {
			throw new HentTilgangDokumentException(DokumentTilgangMessage.FORVALTNINGSNOTAT, "Tilgang til journalpost avvist fordi journalpost er notat, men hoveddokumentet er ikke forvaltningsnotat");
		}
	}

	/**
	 * 1h) Journalposter som har organinterne dokumenter skal ikke vises
	 */
	private void verifyAccessOrganinternJournalpost(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getDokument().getOrganinternt() != null && tilgangJournalpostDto.getDokument().getOrganinternt()) {
			throw new HentTilgangDokumentException(ORGANINTERNT, "Tilgang til journalpost avvist pga organinterne dokumenter på journalposten");
		}
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	private void verifyAccessAndreParterDokument(TilgangJournalpostDto tilgangJournalpostDto, List<String> idents) {
		if ((tilgangJournalpostDto.getJournalpostType() != JournalpostTypeCode.N) &&
				!idents.contains(tilgangJournalpostDto.getAvsenderMottakerId())) {
			throw new HentTilgangDokumentException(ANNEN_PART, "Tilgang til dokument avvist fordi dokumentet er sendt til/fra andre parter enn bruker");
		}
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter
	 */
	private void verifyAccessSkannetDokument(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getMottakskanal() != null && ACCEPTED_MOTTAKS_KANAL.contains(tilgangJournalpostDto.getMottakskanal())) {
			throw new HentTilgangDokumentException(SKANNET_DOKUMENT, "Tilgang til dokument avvist fordi dokumentet er skannet.");
		}
	}

	/**
	 * 2d) Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	 */
	private void verifyAccessInnskrenketPartsinnsynDokument(TilgangDokumentInfoDto tilgangDokumentInfoDto) {
		if ((tilgangDokumentInfoDto.getInnskrenketPartsinnsyn() != null && tilgangDokumentInfoDto.getInnskrenketPartsinnsyn()) ||
				(tilgangDokumentInfoDto.getInnskrenketTredjepart() != null && tilgangDokumentInfoDto.getInnskrenketTredjepart())) {
			throw new HentTilgangDokumentException(INNSKRENKET_PARTSINNSYN, "Tilgang til dokument avvist fordi dokument er markert med innskrenket partsinnsyn");
		}
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. gdpr skal ikke vises
	 */
	private void verfyAccessGDPRDokument(TilgangDokumentInfoDto tilgangDokumentInfoDto) {
		if (GDPR_SKJERMING_TYPE.contains(tilgangDokumentInfoDto.getVariant().getSkjerming())) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til dokument avvist ihht. gdrp");
		}
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	private void verifyAccessKassertDokument(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getDokument().getKassert() != null && tilgangJournalpostDto.getDokument().getKassert()) {
			throw new HentTilgangDokumentException(KASSERT, "Tilgang til dokument avvist fordi dokumentet er kassert");
		}
	}
}
