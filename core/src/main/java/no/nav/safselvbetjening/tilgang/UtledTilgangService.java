package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Innsyn;
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
import java.util.Set;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.domain.Innsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_BRUKERS_ØNSKE;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_FEILSENDT;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.domain.Innsyn.SKJULES_ORGAN_INTERNT;
import static no.nav.safselvbetjening.domain.Innsyn.VISES_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.domain.Innsyn.VISES_MANUELT_GODKJENT;
import static no.nav.safselvbetjening.domain.Innsyn.VISES_MASKINELT_GODKJENT;
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
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_FEILREGISTRERT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_KASSERT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_ORGANINTERNT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_SKJULT_INNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_TEMAER_UNNTATT_INNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_UGYLDIG_JOURNALSTATUS;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_FEILREGISTRERT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_GDPR;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_KASSERT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_ORGANINTERNT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_SKANNET;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_SKJULT;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_TEMAER_UNNTATT_INNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_UGYLDIG_JOURNALSTATUS;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.lagFeilmeldingForDokument;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.lagFeilmeldingForJournalpost;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
@Slf4j
@Component
public class UtledTilgangService {

	private static final EnumSet<SkjermingType> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);
	private static final EnumSet<Kanal> MOTTAKS_KANAL_SKAN = EnumSet.of(SKAN_IM, SKAN_NETS, SKAN_PEN);
	private static final EnumSet<Innsyn> SKJUL_INNSYN = EnumSet.of(SKJULES_BRUKERS_ØNSKE, SKJULES_INNSKRENKET_PARTSINNSYN, SKJULES_FEILSENDT, SKJULES_ORGAN_INTERNT);
	private static final EnumSet<Innsyn> VIS_INNSYN = EnumSet.of(VISES_MASKINELT_GODKJENT, VISES_MANUELT_GODKJENT, VISES_FORVALTNINGSNOTAT);
	private static final EnumSet<Journalstatus> JOURNALSTATUS_FERDIGSTILT = EnumSet.of(FERDIGSTILT, JOURNALFOERT, EKSPEDERT);
	private static final Set<String> TEMA_IKKE_INNSYN_FOR_BRUKER = Tema.brukerHarIkkeInnsynAsString();

	private final LocalDateTime tidligstInnsynDato;

	public UtledTilgangService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.tidligstInnsynDato = safSelvbetjeningProperties.getTidligstInnsynDato().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
	}

	public boolean utledTilgangJournalpost(Journalpost journalpost, BrukerIdenter brukerIdenter) {
		try {
			if (journalpost == null) {
				return false;
			}
			// Med referanse til tilgangsreglene lenket i javadoc.
			return isBrukerPart(journalpost, brukerIdenter) && // 1a
				   !isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost) && // 1b
				   isJournalpostFerdigstiltOrMidlertidig(journalpost) && // 1c
				   !isJournalpostFeilregistrert(journalpost) && // 1d
				   isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost) && // 1e
				   isJournalpostNotGDPRRestricted(journalpost) && // 1f
				   isJournalpostForvaltningsnotat(journalpost) && // 1g
				   isJournalpostNotOrganInternt(journalpost) && // 1h
				   !isJournalpostInnsynSkjult(journalpost.getTilgang()); // 1i
		} catch (Exception e) {
			log.error("Feil oppstått i utledTilgangJournalpost for journalpost med journalpostId={}.", journalpost.getJournalpostId(), e);
			return false;
		}
	}

	public List<String> utledTilgangDokument(Journalpost journalpost, DokumentInfo dokumentInfo, Dokumentvariant dokumentvariant, BrukerIdenter brukerIdenter) {
		List<String> feilmeldinger = new ArrayList<>();

		if (!isAvsenderMottakerPart(journalpost, brukerIdenter.getIdenter())) {
			feilmeldinger.add(DENY_REASON_PARTSINNSYN);
		}
		if (isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost)) {
			feilmeldinger.add(DENY_REASON_INNSYNSDATO);
		}
		if (isSkannetDokumentAndInnsynIsNotVises(journalpost)) {
			feilmeldinger.add(DENY_REASON_SKANNET_DOKUMENT);
		}
		if (isDokumentInnskrenketPartsinnsyn(dokumentInfo)) {
			feilmeldinger.add(DENY_REASON_INNSKRENKET_PARTSINNSYN);
		}
		if (isDokumentGDPRRestricted(dokumentvariant)) {
			feilmeldinger.add(DENY_REASON_GDPR);
		}
		if (isDokumentKassert(dokumentInfo)) {
			feilmeldinger.add(DENY_REASON_KASSERT);
		}
		return feilmeldinger;
	}

	public void utledTilgangHentDokument(Journalpost journalpost, BrukerIdenter brukerIdenter) {

		// Tilgang for journalpost
		if (!isBrukerPart(journalpost, brukerIdenter)) {
			throw new HentTilgangDokumentException(DENY_REASON_PARTSINNSYN, lagFeilmeldingForJournalpost(FEILMELDING_PARTSINNSYN));
		}
		if (isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_INNSYNSDATO, lagFeilmeldingForJournalpost(FEILMELDING_INNSYNSDATO));
		}
		if (!isJournalpostFerdigstiltOrMidlertidig(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_UGYLDIG_JOURNALSTATUS, lagFeilmeldingForJournalpost(FEILMELDING_UGYLDIG_JOURNALSTATUS));
		}
		if (isJournalpostFeilregistrert(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_FEILREGISTRERT, lagFeilmeldingForJournalpost(FEILMELDING_FEILREGISTRERT));
		}
		if (!isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_TEMAER_UNNTATT_INNSYN, lagFeilmeldingForJournalpost(FEILMELDING_TEMAER_UNNTATT_INNSYN));
		}
		if (!isJournalpostNotGDPRRestricted(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_GDPR, lagFeilmeldingForJournalpost(FEILMELDING_GDPR));
		}
		if (!isJournalpostForvaltningsnotat(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_FORVALTNINGSNOTAT, lagFeilmeldingForJournalpost(FEILMELDING_FORVALTNINGSNOTAT));
		}
		if (!isJournalpostNotOrganInternt(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_ORGANINTERNT, lagFeilmeldingForJournalpost(FEILMELDING_ORGANINTERNT));
		}
		if (isJournalpostInnsynSkjult(journalpost.getTilgang())) {
			throw new HentTilgangDokumentException(DENY_REASON_SKJULT_INNSYN, lagFeilmeldingForJournalpost(FEILMELDING_SKJULT));
		}

		// Tilgang for dokument
		if (!isAvsenderMottakerPart(journalpost, brukerIdenter.getIdenter())) {
			throw new HentTilgangDokumentException(DENY_REASON_ANNEN_PART, lagFeilmeldingForDokument(FEILMELDING_ANNEN_PART));
		}
		if (isSkannetDokumentAndInnsynIsNotVises(journalpost)) {
			throw new HentTilgangDokumentException(DENY_REASON_SKANNET_DOKUMENT, lagFeilmeldingForDokument(FEILMELDING_SKANNET));
		}
		if (isDokumentInnskrenketPartsinnsyn(journalpost.getDokumenter().get(0))) {
			throw new HentTilgangDokumentException(DENY_REASON_INNSKRENKET_PARTSINNSYN, lagFeilmeldingForDokument(FEILMELDING_INNSKRENKET_PARTSINNSYN));
		}
		if (isDokumentGDPRRestricted(journalpost.getDokumenter().get(0).getDokumentvarianter().get(0))) {
			throw new HentTilgangDokumentException(DENY_REASON_GDPR, lagFeilmeldingForDokument(FEILMELDING_GDPR));
		}
		if (isDokumentKassert(journalpost.getDokumenter().get(0))) {
			throw new HentTilgangDokumentException(DENY_REASON_KASSERT, lagFeilmeldingForDokument(FEILMELDING_KASSERT));
		}
	}

	/**
	 * 1a) Bruker må være part for å se journalpost
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
	 * 1b) Bruker får ikke se journalposter som er journalført før 04.06.2016 med mindre innsyn begynner med VISES_*.
	 */
	boolean isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(Journalpost journalpost) {
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		if (tilgang.getJournalfoertDato() == null) {
			if (tilgang.getInnsyn() == null || BRUK_STANDARDREGLER.equals(tilgang.getInnsyn())) {
				return tilgang.getDatoOpprettet().isBefore(tidligstInnsynDato);
			}
			return tilgang.getDatoOpprettet().isBefore(tidligstInnsynDato) && !isJournalpostInnsynVises(journalpost.getTilgang());
		} else {
			if (tilgang.getInnsyn() == null || BRUK_STANDARDREGLER.equals(tilgang.getInnsyn())) {
				return (tilgang.getJournalfoertDato().isBefore(tidligstInnsynDato) ||
						tilgang.getDatoOpprettet().isBefore(tidligstInnsynDato));
			}
			return (tilgang.getJournalfoertDato().isBefore(tidligstInnsynDato) ||
					tilgang.getDatoOpprettet().isBefore(tidligstInnsynDato)) && !isJournalpostInnsynVises(journalpost.getTilgang());
		}
	}

	/**
	 * 1c) Bruker får kun se midlertidige eller ferdigstilte journalposter (status M, MO, J, FS, FL eller E)
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
	 * 1e) Bruker får ikke se journalposter på følgende tema:
	 * KTR (Kontroll)
	 * FAR (Farskap)
	 * KTA (Kontroll anmeldelse)
	 * ARS (Arbeidsrådgivning skjermet)
	 * ARP (Arbeidsrådgivning psykologstester)
	 * med mindre k_innsyn = VISES_*
	 */
	public boolean isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(Journalpost journalpost) {
		Journalstatus journalstatus = journalpost.getJournalstatus();

		if (journalstatus != null) {
			Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();

			if (MOTTATT.equals(journalstatus) || (JOURNALSTATUS_FERDIGSTILT.contains(journalstatus) && tilgang.getTilgangSak() == null)) {
				return isJournalpostTemaNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost);
			} else if (tilgang.getTilgangSak() != null && JOURNALSTATUS_FERDIGSTILT.contains(journalstatus)) {
				return isJournalpostTemaOnSakNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost);
			}
		}
		return true;
	}

	/**
	 * 1f) Bruker får ikke se journalposter som er begrenset ihht. GDPR
	 */
	public boolean isJournalpostNotGDPRRestricted(Journalpost journalpost) {
		return !GDPR_SKJERMING_TYPE.contains(journalpost.getTilgang().getSkjerming());
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" eller
	 * innsyn bør begynne med VISES_* for å vise journalposten.
	 */
	public boolean isJournalpostForvaltningsnotat(Journalpost journalpost) {
		List<DokumentInfo> dokumenter = journalpost.getDokumenter();
		Journalpost.TilgangJournalpost tilgang = journalpost.getTilgang();
		if (journalpost.getJournalposttype() == N && !dokumenter.isEmpty() && dokumenter.get(0).getTilgangDokument() != null) {
			boolean isForvaltningsnotat = FORVALTNINGSNOTAT.toString().equals(dokumenter.get(0).getTilgangDokument().getKategori());
			if (tilgang.getInnsyn() == null || BRUK_STANDARDREGLER.equals(tilgang.getInnsyn())) {
				return isForvaltningsnotat;
			}
			return isForvaltningsnotat || isJournalpostInnsynVises(tilgang);
		}
		return true;
	}

	/**
	 * 1h) Journalposter med ett eller flere dokumenter markert som organinternt skal ikke vises
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
	 * 1i) Bruker kan ikke få se journalposter der innsyn begynner med SKJULES_*
	 */
	public boolean isJournalpostInnsynSkjult(Journalpost.TilgangJournalpost tilgang) {
		if (tilgang.getInnsyn() != null) {
			return SKJUL_INNSYN.contains(tilgang.getInnsyn());
		}
		return false;
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	boolean isAvsenderMottakerPart(Journalpost journalpost, List<String> idents) {
		final Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		final String avsenderMottakerId = tilgangJournalpost.getAvsenderMottakerId();
		// Notat er unntatt
		if (journalpost.getJournalposttype() == N) {
			return true;
		}
		if (isNotBlank(avsenderMottakerId)) {
			if (tilgangJournalpost.getInnsyn() != null) {
				return idents.contains(avsenderMottakerId) ? idents.contains(avsenderMottakerId) :
						isJournalpostInnsynVises(journalpost.getTilgang());
			}
			return idents.contains(avsenderMottakerId);
		}
		return false;
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter med mindre innsyn begynner med VISES_*
	 */
	boolean isSkannetDokumentAndInnsynIsNotVises(Journalpost journalpost) {
		Kanal mottakskanal = journalpost.getTilgang().getMottakskanal();
		Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		if (mottakskanal != null) {
			if (tilgangJournalpost.getInnsyn() != null && (MOTTAKS_KANAL_SKAN.contains(mottakskanal))) {
				return !isJournalpostInnsynVises(tilgangJournalpost);
			}
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
	 * 2e) Dokumenter som er begrenset ihht. GDPR skal ikke vises
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

	boolean isJournalpostTemaOnSakNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(Journalpost journalpost) {
		boolean isTemaUnntattInnsyn = TEMA_IKKE_INNSYN_FOR_BRUKER.contains(journalpost.getTilgang().getTilgangSak().getTema());
		Journalpost.TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
		if (tilgangJournalpost.getInnsyn() != null && isTemaUnntattInnsyn) {
			return isJournalpostInnsynVises(journalpost.getTilgang());
		}
		return !isTemaUnntattInnsyn;
	}

	boolean isJournalpostTemaNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(Journalpost journalpost) {
		boolean isTemaUnntattInnsyn = TEMA_IKKE_INNSYN_FOR_BRUKER.contains(journalpost.getTilgang().getTema());
		if (journalpost.getTilgang().getInnsyn() != null && isTemaUnntattInnsyn) {
			return isJournalpostInnsynVises(journalpost.getTilgang());
		}
		return !isTemaUnntattInnsyn;
	}

	public boolean isJournalpostInnsynVises(Journalpost.TilgangJournalpost tilgang) {
		return tilgang.getInnsyn() != null && VIS_INNSYN.contains(tilgang.getInnsyn());
	}

}
