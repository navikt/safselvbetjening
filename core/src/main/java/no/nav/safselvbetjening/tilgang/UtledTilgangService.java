package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
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
import java.util.EnumSet;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
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

	private static final EnumSet<SkjermingType> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);
	private static final List<Kanal> ACCEPTED_MOTTAKS_KANAL = List.of(SKAN_IM, SKAN_NETS, SKAN_PEN);
	private static final EnumSet<Journalstatus> JOURNALSTATUS_FERDIGSTILT = EnumSet.of(FERDIGSTILT, JOURNALFOERT, EKSPEDERT);

	private final LocalDateTime tidligstInnsynDato;

	public UtledTilgangService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.tidligstInnsynDato = safSelvbetjeningProperties.getTidligstInnsynDato().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
	}

	public List<String> utledTilgangDokument(Journalpost journalpost, DokumentInfo dokumentInfo, Dokumentvariant dokumentvariant, BrukerIdenter brukerIdenter) {
		List<String> feilmeldinger = new ArrayList<>();

		if (!isAvsenderMottakerPart(journalpost, brukerIdenter.getIdenter())) {
			feilmeldinger.add(PARTSINNSYN);
		}
		if (!isJournalfoertDatoAfterInnsynsdato(journalpost)) {
			feilmeldinger.add(INNSYNSDATO);
		}
		if (isSkannetDokument(journalpost)) {
			feilmeldinger.add(SKANNET_DOKUMENT);
		}
		if (isDokumentInnskrenketPartsinnsyn(dokumentInfo.getTilgangDokument())) {
			feilmeldinger.add(INNSKRENKET_PARTSINNSYN);
		}
		if (isDokumentGDPRRestricted(dokumentvariant.getTilgangVariant())) {
			feilmeldinger.add(GDPR);
		}
		if (isDokumentKassert(dokumentInfo.getTilgangDokument())) {
			feilmeldinger.add(KASSERT);
		}

		return feilmeldinger;
	}

	public void utledTilgangHentDokument(Journalpost journalpost, BrukerIdenter brukerIdenter) {
		if (!isBrukerPart(journalpost, brukerIdenter)) {
			throw new HentTilgangDokumentException(PARTSINNSYN, "Tilgang til journalpost avvist fordi bruker ikke er part");
		}
		if (!isJournalfoertDatoAfterInnsynsdato(journalpost)) {
			throw new HentTilgangDokumentException(INNSYNSDATO, "Tilgang til journalpost avvist fordi journalposten er opprettet før tidligst innsynsdato");
		}
		if (!isJournalpostFerdigstiltOrMidlertidig(journalpost)) {
			throw new HentTilgangDokumentException(UGYLDIG_JOURNALSTATUS, "Tilgang til journalpost avvist fordi journalpost er ikke ferdigstilt eller midlertidig");
		}
		if (isJournalpostFeilregistrert(journalpost)) {
			throw new HentTilgangDokumentException(FEILREGISTRERT, "Tilgang til journalpost avvist fordi journalpost er feilregistrert");
		}
		if (!isJournalpostNotKontrollsak(journalpost)) {
			throw new HentTilgangDokumentException(KONTROLLSAK, "Tilgang til journalpost avvist fordi journalpost er markert som kontrollsak");
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
		if (!isAvsenderMottakerPart(journalpost, brukerIdenter.getIdenter())) {
			throw new HentTilgangDokumentException(ANNEN_PART, "Tilgang til dokument avvist fordi dokumentet er sendt til/fra andre parter enn bruker");
		}
		if (isSkannetDokument(journalpost)) {
			throw new HentTilgangDokumentException(SKANNET_DOKUMENT, "Tilgang til dokument avvist fordi dokumentet er skannet.");
		}
		if (isDokumentInnskrenketPartsinnsyn(journalpost.getDokumenter().get(0).getTilgangDokument())) {
			throw new HentTilgangDokumentException(INNSKRENKET_PARTSINNSYN, "Tilgang til dokument avvist fordi dokument er markert med innskrenket partsinnsyn");
		}
		if (isDokumentGDPRRestricted(journalpost.getDokumenter().get(0).getDokumentvarianter().get(0).getTilgangVariant())) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til dokument avvist ihht. gdrp");
		}
		if (isDokumentKassert(journalpost.getDokumenter().get(0).getTilgangDokument())) {
			throw new HentTilgangDokumentException(KASSERT, "Tilgang til dokument avvist fordi dokumentet er kassert");
		}
	}

	/**
	 * 1a) Bruker må være part for å se journalposter
	 */
	public boolean isBrukerPart(Journalpost journalpost, BrukerIdenter identer) {

		Journalstatus journalstatus = journalpost.getJournalstatus();

		if (MOTTATT.equals(journalstatus)) {
			return identer.getIdenter().contains(journalpost.getTilgang().getTilgangBruker().getBrukerId());
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalstatus)) {
			if (FS22.toString().equals(journalpost.getTilgang().getTilgangSak().getFagsystem())) {
				return identer.getIdenter().contains(journalpost.getTilgang().getTilgangSak().getAktoerId());
			} else if (FagsystemCode.PEN.toString().equals(journalpost.getTilgang().getTilgangSak().getFagsystem())) {
				return identer.getFoedselsnummer().contains(journalpost.getTilgang().getTilgangBruker().getBrukerId());
			}
		}
		return false;
	}

	/**
	 * 1b) Bruker får ikke se journalposter som er journalført før 04.06.2016
	 */
	boolean isJournalfoertDatoAfterInnsynsdato(Journalpost journalpost) {
		if (journalpost.getTilgang().getJournalfoertDato() == null) {
			return true;
		} else {
			return journalpost.getTilgang().getJournalfoertDato().isAfter(tidligstInnsynDato);
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
		return journalpost.getTilgang().getTilgangSak().isFeilregistrert();
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	public boolean isJournalpostNotKontrollsak(Journalpost journalpost) {
		Journalstatus journalstatus = journalpost.getJournalstatus();

		if (MOTTATT.equals(journalstatus)) {
			return FagomradeCode.KTR.toString().equals(journalpost.getTilgang().getFagomradeCode());
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalstatus) &&
				journalpost.getTilgang().getTilgangSak() != null) {
			return Tema.KTR.toString().equals(journalpost.getTilgang().getTilgangSak().getTema());
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalstatus) &&
				journalpost.getTilgang().getTilgangSak() == null) {
			return FagomradeCode.KTR.toString().equals(journalpost.getTilgang().getFagomradeCode());
		}
		return true;
	}

	/**
	 * 1f) Bruker kan ikke få se journalposter som er begrenset ihht. gdpr
	 */
	public boolean isJournalpostNotGDPRRestricted(Journalpost journalpost) {
		return GDPR_SKJERMING_TYPE.contains(journalpost.getTilgang().getSkjerming());
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" for å vise journalposten.
	 */
	public boolean isJournalpostForvaltningsnotat(Journalpost journalpost) {
		if (journalpost.getJournalposttype() == N) {
			return FORVALTNINGSNOTAT.toString().equals(journalpost.getDokumenter().get(0).getTilgangDokument().getKategori());
		}
		return true;
	}

	/**
	 * 1h) Journalposter som har organinterne dokumenter skal ikke vises
	 */
	public boolean isJournalpostNotOrganInternt(Journalpost journalpost) {
		return journalpost.getDokumenter().stream().noneMatch(dokumentInfo -> dokumentInfo.getTilgangDokument().isOrganinternt());
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	boolean isAvsenderMottakerPart(Journalpost journalpost, List<String> idents) {
		if (journalpost.getJournalposttype() != N) {
			return idents.contains(journalpost.getAvsenderMottaker().getId());
		}
		return true;
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter
	 */
	boolean isSkannetDokument(Journalpost journalpost) {
		return ACCEPTED_MOTTAKS_KANAL.contains(journalpost.getKanal());
	}

	/**
	 * 2d) Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	 */
	boolean isDokumentInnskrenketPartsinnsyn(DokumentInfo.TilgangDokument tilgangDokument) {
		return (tilgangDokument.isInnskrenketPartsinnsyn() || tilgangDokument.isInnskrenketTredjepart());
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. gdpr skal ikke vises
	 */
	boolean isDokumentGDPRRestricted(Dokumentvariant.TilgangVariant tilgangVariant) {
		return GDPR_SKJERMING_TYPE.contains(tilgangVariant.getSkjerming());
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	boolean isDokumentKassert(DokumentInfo.TilgangDokument tilgangDokument) {
		return tilgangDokument.isKassert();
	}
}
