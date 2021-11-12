package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.domain.Journalposttype.N;
import static no.nav.safselvbetjening.domain.Journalstatus.EKSPEDERT;
import static no.nav.safselvbetjening.domain.Journalstatus.FERDIGSTILT;
import static no.nav.safselvbetjening.domain.Journalstatus.JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Journalstatus.MOTTATT;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_IM;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_NETS;
import static no.nav.safselvbetjening.domain.Kanal.SKAN_PEN;
import static no.nav.safselvbetjening.domain.SkjermingType.FEIL;
import static no.nav.safselvbetjening.domain.SkjermingType.POL;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.FEILREGISTRERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KASSERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KONTROLLSAK_FARSKAPSSAK;
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

	private static final EnumSet<SkjermingType> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);
	private static final EnumSet<Kanal> MOTTAKS_KANAL_SKAN = EnumSet.of(SKAN_IM, SKAN_NETS, SKAN_PEN);
	private static final EnumSet<Journalstatus> JOURNALSTATUS_FERDIGSTILT = EnumSet.of(FERDIGSTILT, JOURNALFOERT, EKSPEDERT);
	private static final List<String> TEMAER_UNTATT_VISNING = Arrays.asList(Tema.KTR.name(), Tema.FAR.name());
	private static final List<String> FAGOMRADER_UNTATT_VISNING = Arrays.asList(FagomradeCode.KTR.name(), FagomradeCode.FAR.name());

	private final LocalDateTime tidligstInnsynDato;

	public UtledTilgangService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.tidligstInnsynDato = safSelvbetjeningProperties.getTidligstInnsynDato().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
	}

	public boolean utledTilgangJournalpost(Journalpost journalpost, BrukerIdenter brukerIdenter) {
		try {
			if (journalpost == null) {
				return false;
			}

			return isBrukerPart(journalpost, brukerIdenter) && isJournalpostNotGDPRRestricted(journalpost) &&
					isJournalpostNotKontrollsakOrFarskapssak(journalpost) && isJournalpostForvaltningsnotat(journalpost) &&
					isJournalpostNotOrganInternt(journalpost);
		} catch (Exception e) {
			log.error("Feil oppstått i utledTilgangJournalpost for journalpost med journalpostId={}.", journalpost.getJournalpostId(), e);
			return false;
		}
	}

	public List<String> utledTilgangDokument(Journalpost journalpost, DokumentInfo dokumentInfo, Dokumentvariant dokumentvariant, BrukerIdenter brukerIdenter) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isAvsenderMottakerNotPart(journalpost, brukerIdenter.getIdenter())) {
			feilmeldinger.add(PARTSINNSYN);
		}
		if (isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdato(journalpost)) {
			feilmeldinger.add(INNSYNSDATO);
		}
		if (isSkannetDokument(journalpost)) {
			feilmeldinger.add(SKANNET_DOKUMENT);
		}
		if (isDokumentInnskrenketPartsinnsyn(dokumentInfo)) {
			feilmeldinger.add(INNSKRENKET_PARTSINNSYN);
		}
		if (isDokumentGDPRRestricted(dokumentvariant)) {
			feilmeldinger.add(GDPR);
		}
		if (isDokumentKassert(dokumentInfo)) {
			feilmeldinger.add(KASSERT);
		}
		return feilmeldinger;
	}

	public void utledTilgangHentDokument(Journalpost journalpost, BrukerIdenter brukerIdenter) {
		if (!isBrukerPart(journalpost, brukerIdenter)) {
			throw new HentTilgangDokumentException(PARTSINNSYN, "Tilgang til journalpost avvist fordi bruker ikke er part");
		}
		if (isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdato(journalpost)) {
			throw new HentTilgangDokumentException(INNSYNSDATO, "Tilgang til journalpost avvist fordi journalposten er opprettet før tidligst innsynsdato");
		}
		if (!isJournalpostFerdigstiltOrMidlertidig(journalpost)) {
			throw new HentTilgangDokumentException(UGYLDIG_JOURNALSTATUS, "Tilgang til journalpost avvist fordi journalpost er ikke ferdigstilt eller midlertidig");
		}
		if (isJournalpostFeilregistrert(journalpost)) {
			throw new HentTilgangDokumentException(FEILREGISTRERT, "Tilgang til journalpost avvist fordi journalpost er feilregistrert");
		}
		if (!isJournalpostNotKontrollsakOrFarskapssak(journalpost)) {
			throw new HentTilgangDokumentException(KONTROLLSAK_FARSKAPSSAK, "Tilgang til journalpost avvist fordi journalpost er markert som kontrollsak eller farskapssak");
		}
		if (!isJournalpostNotGDPRRestricted(journalpost)) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til journalpost avvist ihht. gdpr");
		}
		if (!isJournalpostForvaltningsnotat(journalpost)) {
			throw new HentTilgangDokumentException(DokumentTilgangMessage.FORVALTNINGSNOTAT, "Tilgang til journalpost avvist fordi journalpost er notat, men hoveddokumentet er ikke forvaltningsnotat");
		}
		if (!isJournalpostNotOrganInternt(journalpost)) {
			throw new HentTilgangDokumentException(ORGANINTERNT, "Tilgang til journalpost avvist pga organinterne dokumenter på journalposten");
		}
		if (isAvsenderMottakerNotPart(journalpost, brukerIdenter.getIdenter())) {
			throw new HentTilgangDokumentException(ANNEN_PART, "Tilgang til dokument avvist fordi dokumentet er sendt til/fra andre parter enn bruker");
		}
		if (isSkannetDokument(journalpost)) {
			throw new HentTilgangDokumentException(SKANNET_DOKUMENT, "Tilgang til dokument avvist fordi dokumentet er skannet.");
		}
		if (isDokumentInnskrenketPartsinnsyn(journalpost.getDokumenter().get(0))) {
			throw new HentTilgangDokumentException(INNSKRENKET_PARTSINNSYN, "Tilgang til dokument avvist fordi dokument er markert med innskrenket partsinnsyn");
		}
		if (isDokumentGDPRRestricted(journalpost.getDokumenter().get(0).getDokumentvarianter().get(0))) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til dokument avvist ihht. gdrp");
		}
		if (isDokumentKassert(journalpost.getDokumenter().get(0))) {
			throw new HentTilgangDokumentException(KASSERT, "Tilgang til dokument avvist fordi dokumentet er kassert");
		}
	}

	/**
	 * 1a) Bruker må være part for å se journalposter
	 */
	public boolean isBrukerPart(Journalpost journalpost, BrukerIdenter identer) {
		Journalstatus journalstatus = journalpost.getJournalstatus();
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		if (MOTTATT.equals(journalstatus)) {
			Journalpost.TilgangBruker tilgangBruker = tilgang.getTilgangBruker();
			if (tilgangBruker != null) {
				return identer.getIdenter().contains(tilgangBruker.getBrukerId());
			}
		} else {
			Journalpost.TilgangSak tilgangSak = tilgang.getTilgangSak();
			if (tilgangSak != null && JOURNALSTATUS_FERDIGSTILT.contains(journalstatus)) {
				if (FS22.toString().equals(tilgangSak.getFagsystem())) {
					return identer.getIdenter().contains(tilgangSak.getAktoerId());
				} else if (PEN.name().equals(tilgangSak.getFagsystem())) {
					return identer.getIdenter().contains(tilgangSak.getFoedselsnummer());
				}
			}
		}
		return false;
	}

	/**
	 * 1b) Bruker får ikke se journalposter som er journalført før 04.06.2016
	 */
	boolean isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdato(Journalpost journalpost) {
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		if (tilgang.getJournalfoertDato() == null) {
			return tilgang.getDatoOpprettet().isBefore(tidligstInnsynDato);
		} else {
			return tilgang.getJournalfoertDato().isBefore(tidligstInnsynDato) ||
					tilgang.getDatoOpprettet().isBefore(tidligstInnsynDato);
		}
	}

	/**
	 * 1c) Bruker får kun se ferdigstilte journalposter
	 */
	boolean isJournalpostFerdigstiltOrMidlertidig(Journalpost journalpost) {
		return JOURNALSTATUS_FERDIGSTILT.contains(journalpost.getJournalstatus()) || MOTTATT.equals(journalpost.getJournalstatus());
	}

	/**
	 * 1d) Bruker får ikke se feilregistrerte journalposter
	 */
	boolean isJournalpostFeilregistrert(Journalpost journalpost) {
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		if (tilgang.getTilgangSak() != null) {
			return tilgang.getTilgangSak().isFeilregistrert();
		}
		return false;
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker eller farskapssaker
	 */
	public boolean isJournalpostNotKontrollsakOrFarskapssak(Journalpost journalpost) {
		Journalstatus journalstatus = journalpost.getJournalstatus();

		if (journalstatus != null) {
			Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
			if (MOTTATT.equals(journalstatus)) {
				return ! FAGOMRADER_UNTATT_VISNING.contains(tilgang.getTema());
			} else if (tilgang.getTilgangSak() != null && JOURNALSTATUS_FERDIGSTILT.contains(journalstatus)) {
				return ! TEMAER_UNTATT_VISNING.contains(tilgang.getTilgangSak().getTema());
			} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalstatus) && tilgang.getTilgangSak() == null) {
				return ! FAGOMRADER_UNTATT_VISNING.contains(tilgang.getTema());
			}
		}
		return true;
	}

	/**
	 * 1f) Bruker kan ikke få se journalposter som er begrenset ihht. gdpr
	 */
	public boolean isJournalpostNotGDPRRestricted(Journalpost journalpost) {
		return !GDPR_SKJERMING_TYPE.contains(journalpost.getTilgang().getSkjerming());
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" for å vise journalposten.
	 */
	public boolean isJournalpostForvaltningsnotat(Journalpost journalpost) {
		List<DokumentInfo> dokumenter = journalpost.getDokumenter();
		if (journalpost.getJournalposttype() == N && !dokumenter.isEmpty() && dokumenter.get(0).getTilgangDokument() != null) {
			return FORVALTNINGSNOTAT.toString().equals(dokumenter.get(0).getTilgangDokument().getKategori());
		}
		return true;
	}

	/**
	 * 1h) Journalposter som har organinterne dokumenter skal ikke vises
	 */
	public boolean isJournalpostNotOrganInternt(Journalpost journalpost) {
		if (!journalpost.getDokumenter().isEmpty()) {
			for (DokumentInfo dokumentInfo : journalpost.getDokumenter()) {
				if (dokumentInfo.getTilgangDokument() != null && dokumentInfo.getTilgangDokument().isOrganinternt()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	boolean isAvsenderMottakerNotPart(Journalpost journalpost, List<String> idents) {
		final Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		final String avsenderMottakerId = tilgangJournalpost.getAvsenderMottakerId();
		if (journalpost.getJournalposttype() != N && avsenderMottakerId != null) {
			return !idents.contains(avsenderMottakerId);
		}
		return false;
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter
	 */
	boolean isSkannetDokument(Journalpost journalpost) {
		Kanal mottakskanal = journalpost.getTilgang().getMottakskanal();
		if (mottakskanal != null) {
			return MOTTAKS_KANAL_SKAN.contains(mottakskanal);
		}
		return false;
	}

	/**
	 * 2d) Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	 */
	boolean isDokumentInnskrenketPartsinnsyn(DokumentInfo dokumentInfo) {
		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		if (tilgangDokument != null) {
			return (tilgangDokument.isInnskrenketPartsinnsyn() || tilgangDokument.isInnskrenketTredjepart());
		}
		return false;
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. gdpr skal ikke vises
	 */
	boolean isDokumentGDPRRestricted(Dokumentvariant dokumentvariant) {
		Dokumentvariant.TilgangVariant tilgangVariant = dokumentvariant.getTilgangVariant();
		if (tilgangVariant != null) {
			return GDPR_SKJERMING_TYPE.contains(tilgangVariant.getSkjerming());
		}
		return false;
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	boolean isDokumentKassert(DokumentInfo dokumentInfo) {
		DokumentInfo.TilgangDokument tilgangDokument = dokumentInfo.getTilgangDokument();
		if (tilgangDokument != null) {
			return tilgangDokument.isKassert();
		}
		return false;
	}
}
