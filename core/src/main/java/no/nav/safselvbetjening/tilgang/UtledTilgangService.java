package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangDokument;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangJournalpost;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangVariant;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
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
		if (!isBrukerPart(utledTilgangJournalpost, brukerIdenter)) {
			throw new HentTilgangDokumentException(PARTSINNSYN, "Tilgang til journalpost avvist fordi bruker ikke er part");
		}
		if (!isJournalfoertDatoAfterInnsynsdato(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(INNSYNSDATO, "Tilgang til journalpost avvist fordi journalposten er opprettet før tidligst innsynsdato");
		}
		if (!isJournalpostFerdigstiltOrMidlertidig(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(UGYLDIG_JOURNALSTATUS, "Tilgang til journalpost avvist fordi journalpost er ikke ferdigstilt eller midlertidig");
		}
		if (isJournalpostFeilregistrert(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(FEILREGISTRERT, "Tilgang til journalpost avvist fordi journalpost er feilregistrert");
		}
		if (isJournalpostKontrollsak(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(KONTROLLSAK, "Tilgang til journalpost avvist fordi journalpost er markert som kontrollsak");
		}
		if (isJournalpostGDPRRestricted(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til journalpost avvist ihht. gdpr");
		}
		if (!isJournalpostForvaltningsnotat(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(DokumentTilgangMessage.FORVALTNINGSNOTAT, "Tilgang til journalpost avvist fordi journalpost er notat, men hoveddokumentet er ikke forvaltningsnotat");
		}
		if (!isNotJournalpostOrganInternt(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(ORGANINTERNT, "Tilgang til journalpost avvist pga organinterne dokumenter på journalposten");
		}
		if (!isAvsenderMottakerPart(utledTilgangJournalpost, brukerIdenter.getIdenter())) {
			throw new HentTilgangDokumentException(ANNEN_PART, "Tilgang til dokument avvist fordi dokumentet er sendt til/fra andre parter enn bruker");
		}
		if (isSkannetDokument(utledTilgangJournalpost)) {
			throw new HentTilgangDokumentException(SKANNET_DOKUMENT, "Tilgang til dokument avvist fordi dokumentet er skannet.");
		}
		if (isDokumentInnskrenketPartsinnsyn(utledTilgangJournalpost.getUtledTilgangDokumentList().get(0))) {
			throw new HentTilgangDokumentException(INNSKRENKET_PARTSINNSYN, "Tilgang til dokument avvist fordi dokument er markert med innskrenket partsinnsyn");
		}
		if (isDokumentGDPRRestricted(utledTilgangJournalpost.getUtledTilgangDokumentList().get(0).getVariantList().get(0))) {
			throw new HentTilgangDokumentException(GDPR, "Tilgang til dokument avvist ihht. gdrp");
		}
		if (isDokumentKassert(utledTilgangJournalpost.getUtledTilgangDokumentList().get(0))) {
			throw new HentTilgangDokumentException(KASSERT, "Tilgang til dokument avvist fordi dokumentet er kassert");
		}
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
	private boolean isJournalfoertDatoAfterInnsynsdato(UtledTilgangJournalpost utledTilgangJournalpost) {
		if (utledTilgangJournalpost.getJournalfoertDato() == null) {
			return true;
		} else {
			return utledTilgangJournalpost.getJournalfoertDato().isAfter(tidligstInnsynDato);
		}
	}

	/**
	 * 1c) Bruker får kun se ferdigstilte journalposter
	 */
	private boolean isJournalpostFerdigstiltOrMidlertidig(UtledTilgangJournalpost utledTilgangJournalpost) {
		return JOURNALSTATUS_FERDIGSTILT.contains(utledTilgangJournalpost.getJournalstatusCode()) || JOURNALSTATUS_MIDLERTIDIG.contains(utledTilgangJournalpost.getJournalstatusCode());
	}

	/**
	 * 1d) Bruker får ikke se feilregistrerte journalposter
	 */
	private boolean isJournalpostFeilregistrert(UtledTilgangJournalpost utledTilgangJournalpost) {
		return utledTilgangJournalpost.isFeilregistrert();
	}

	/**
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private boolean isJournalpostKontrollsak(UtledTilgangJournalpost utledTilgangJournalpost) {
		JournalStatusCode journalStatusCode = utledTilgangJournalpost.getJournalstatusCode();

		if (JOURNALSTATUS_MIDLERTIDIG.contains(journalStatusCode)) {
			return utledTilgangJournalpost.getFagomradeCode() == FagomradeCode.KTR;
		} else if (JOURNALSTATUS_FERDIGSTILT.contains(journalStatusCode)) {
			if (utledTilgangJournalpost.getUtledTilgangSak() != null) {
				return Tema.KTR.toString().equals(utledTilgangJournalpost.getUtledTilgangSak().getTema());
			} else {
				return utledTilgangJournalpost.getFagomradeCode() == FagomradeCode.KTR;
			}
		}
		return true;
	}

	/**
	 * 1f) Bruker kan ikke få se journalposter som er begrenset ihht. gdpr
	 */
	private boolean isJournalpostGDPRRestricted(UtledTilgangJournalpost utledTilgangJournalpost) {
		return GDPR_SKJERMING_TYPE.contains(utledTilgangJournalpost.getSkjerming());
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" for å vise journalposten.
	 */
	private boolean isJournalpostForvaltningsnotat(UtledTilgangJournalpost utledTilgangJournalpost) {
		if (utledTilgangJournalpost.getJournalpostType() == N) {
			return FORVALTNINGSNOTAT.equals(utledTilgangJournalpost.getUtledTilgangDokumentList().get(0).getKategori());
		}
		return true;
	}

	/**
	 * 1h) Journalposter som har organinterne dokumenter skal ikke vises
	 */
	private boolean isNotJournalpostOrganInternt(UtledTilgangJournalpost utledTilgangJournalpost) {
		return utledTilgangJournalpost.getUtledTilgangDokumentList().stream().noneMatch(UtledTilgangDokument::isOrganinternt);
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	private boolean isAvsenderMottakerPart(UtledTilgangJournalpost utledTilgangJournalpost, List<String> idents) {
		if (utledTilgangJournalpost.getJournalpostType() != JournalpostTypeCode.N) {
			return idents.contains(utledTilgangJournalpost.getAvsenderMottakerId());
		}
		return true;
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter
	 */
	private boolean isSkannetDokument(UtledTilgangJournalpost utledTilgangJournalpost) {
		return ACCEPTED_MOTTAKS_KANAL.contains(utledTilgangJournalpost.getMottaksKanalCode());
	}

	/**
	 * 2d) Dokumenter markert som innskrenketPartsinnsyn skal ikke vises
	 */
	private boolean isDokumentInnskrenketPartsinnsyn(UtledTilgangDokument utledTilgangDokument) {
		return (utledTilgangDokument.isInnskrenketPartsinnsyn() || utledTilgangDokument.isInnskrenketTredjepart());
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. gdpr skal ikke vises
	 */
	private boolean isDokumentGDPRRestricted(UtledTilgangVariant utledTilgangVariant) {
		return GDPR_SKJERMING_TYPE.contains(utledTilgangVariant.getSkjerming());
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	private boolean isDokumentKassert(UtledTilgangDokument utledTilgangDokument) {
		return utledTilgangDokument.isKassert();
	}
}
