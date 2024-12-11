package no.nav.safselvbetjening.tilgang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_ANNEN_PART;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_FEILREGISTRERT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_GDPR;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_KASSERT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_SKJULT_INNSYN;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_TEMAER_UNNTATT_INNSYN;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_UGYLDIG_JOURNALSTATUS;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_UGYLDIG_VARIANTFORMAT;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.BRUK_STANDARDREGLER;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.FERDIGSTILT;
import static no.nav.safselvbetjening.tilgang.TilgangJournalstatus.MOTTATT;

/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
public class UtledTilgangService {
	public static final LocalDateTime TIDLIGST_INNSYN_DATO = LocalDate.of(2016, 6, 4).atStartOfDay();
	private static final String FORVALTNINGSNOTAT = "FORVALTNINGSNOTAT";
	private static final Set<String> GJELDENDE_TEMA_UNNTATT_INNSYN = Set.of("FAR", "KTR", "KTA", "ARS", "ARP");

	/**
	 * Sjekk om bruker har tilgang til å se en gitt journalpost. Merk: å få tilgang her indikerer ingenting om hvorvidt journalposten har dokumentvarianter brukeren faktisk kan se.
	 *
	 * @param journalpost   journalposten
	 * @param brukerIdenter en liste med brukerens gyldige identer
	 * @return en liste med grunner til at brukeren ikke kan se journalposten. Om listen er tom skal brukeren ha tilgang.
	 */
	public List<TilgangDenyReason> utledTilgangJournalpost(TilgangJournalpost journalpost, Set<Ident> brukerIdenter) {
		List<TilgangDenyReason> feilmeldinger = new ArrayList<>();

		// Med referanse til tilgangsreglene lenket i javadoc.
		if (!isBrukerPart(journalpost, brukerIdenter)) { // 1a
			feilmeldinger.add(DENY_REASON_ANNEN_PART);
		}
		if (isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost)) { // 1b
			feilmeldinger.add(DENY_REASON_INNSYNSDATO);
		}
		if (!isJournalpostFerdigstiltOrMidlertidig(journalpost)) { // 1c
			feilmeldinger.add(DENY_REASON_UGYLDIG_JOURNALSTATUS);
		}
		if (isJournalpostFeilregistrert(journalpost)) { // 1d
			feilmeldinger.add(DENY_REASON_FEILREGISTRERT);
		}
		if (!isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(journalpost)) { // 1e
			feilmeldinger.add(DENY_REASON_TEMAER_UNNTATT_INNSYN);
		}
		if (isJournalpostGDPRRestricted(journalpost)) { // 1f
			feilmeldinger.add(DENY_REASON_GDPR);
		}
		if (!isJournalpostNotatXNORForvaltningsnotat(journalpost)) { // 1g
			feilmeldinger.add(DENY_REASON_FORVALTNINGSNOTAT);
		}
		if (isJournalpostInnsynSkjules(journalpost)) { // 1i
			feilmeldinger.add(DENY_REASON_SKJULT_INNSYN);
		}
		return feilmeldinger;
	}

	/**
	 * Sjekk om bruker har tilgang til en gitt dokumentvariant av et dokument i en journalpost
	 *
	 * @param journalpost    journalposten dokumentet tilhører
	 * @param dokumentInfo   dokumentet som eier dokumentvarianten
	 * @param tilgangVariant dokumentvarianten
	 * @param brukerIdenter  en liste med brukerens gyldige identer
	 * @return en liste med grunner til at brukeren ikke kan se dokumentet. Om listen er tom skal brukeren ha tilgang
	 */
	public List<TilgangDenyReason> utledTilgangDokument(TilgangJournalpost journalpost, TilgangDokument dokumentInfo, TilgangVariant tilgangVariant, Set<Ident> brukerIdenter) {
		List<TilgangDenyReason> feilmeldinger = new ArrayList<>();

		if (!isAvsenderMottakerPart(journalpost, brukerIdenter)) {
			feilmeldinger.add(DENY_REASON_ANNEN_PART);
		}
		if (isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(journalpost)) {
			feilmeldinger.add(DENY_REASON_INNSYNSDATO);
		}
		if (isSkannetDokumentAndInnsynIsNotVises(journalpost)) {
			feilmeldinger.add(DENY_REASON_SKANNET_DOKUMENT);
		}
		if (isDokumentGDPRRestricted(dokumentInfo)) {
			feilmeldinger.add(DENY_REASON_GDPR);
		}
		if (isDokumentvariantGDPRRestricted(tilgangVariant)) {
			feilmeldinger.add(DENY_REASON_GDPR);
		}
		if (isDokumentKassert(dokumentInfo)) {
			feilmeldinger.add(DENY_REASON_KASSERT);
		}
		if (isNotGyldigVariant(tilgangVariant)) {
			feilmeldinger.add(DENY_REASON_UGYLDIG_VARIANTFORMAT);
		}

		return feilmeldinger;
	}


	private static boolean isNotGyldigVariant(TilgangVariant tilgangVariant) {
		return tilgangVariant == null || !tilgangVariant.variantformat().gyldigForInnsyn;
	}

	/**
	 * 1a) Bruker må være part for å se journalpost
	 */
	boolean isBrukerPart(TilgangJournalpost tilgangJournalpost, Set<Ident> identer) {
		TilgangJournalstatus journalstatus = tilgangJournalpost.getJournalstatus();
		if (MOTTATT == journalstatus) {
			TilgangBruker tilgangBruker = tilgangJournalpost.getTilgangBruker();
			if (tilgangBruker != null) {
				return identer.contains(tilgangBruker.brukerId());
			}
		} else {
			if (FERDIGSTILT == journalstatus) {
				TilgangSak tilgangSak = tilgangJournalpost.getTilgangSak();
				if (tilgangSak != null) {
					return tilgangSak.getIdent() != null && identer.contains(tilgangSak.getIdent());
				}
			}
		}
		return false;
	}

	/**
	 * 1b) Bruker får ikke se journalposter som er journalført før 04.06.2016 med mindre innsyn begynner med VISES_*.
	 */
	boolean isJournalfoertDatoOrOpprettetDatoBeforeInnsynsdatoAndInnsynIsNotVises(TilgangJournalpost journalpost) {
		if (journalpost.getJournalfoertDato() == null) {
			if (BRUK_STANDARDREGLER == journalpost.getInnsyn()) {
				return journalpost.getDatoOpprettet().isBefore(TIDLIGST_INNSYN_DATO);
			}
			return journalpost.getDatoOpprettet().isBefore(TIDLIGST_INNSYN_DATO) && !journalpost.innsynVises();
		} else {
			if (BRUK_STANDARDREGLER == journalpost.getInnsyn()) {
				return (journalpost.getJournalfoertDato().isBefore(TIDLIGST_INNSYN_DATO) ||
						journalpost.getDatoOpprettet().isBefore(TIDLIGST_INNSYN_DATO));
			}
			return (journalpost.getJournalfoertDato().isBefore(TIDLIGST_INNSYN_DATO) ||
					journalpost.getDatoOpprettet().isBefore(TIDLIGST_INNSYN_DATO)) && !journalpost.innsynVises();
		}
	}

	/**
	 * 1c) Bruker får kun se midlertidige eller ferdigstilte journalposter (status M, MO, J, FS, FL eller E)
	 */
	boolean isJournalpostFerdigstiltOrMidlertidig(TilgangJournalpost journalpost) {
		return FERDIGSTILT == journalpost.getJournalstatus() || MOTTATT == journalpost.getJournalstatus();
	}

	/**
	 * 1d) Bruker får ikke se feilregistrerte journalposter
	 */
	boolean isJournalpostFeilregistrert(TilgangJournalpost journalpost) {
		if (journalpost.getTilgangSak() == null) {
			return false;
		}
		return journalpost.getTilgangSak().isFeilregistrert();
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
	boolean isJournalpostNotUnntattInnsynOrInnsynVistForTemaUnntattInnsyn(TilgangJournalpost journalpost) {
		TilgangJournalstatus journalstatus = journalpost.getJournalstatus();

		if (journalstatus == null) {
			return true;
		}
		boolean isTemaUnntattInnsyn = GJELDENDE_TEMA_UNNTATT_INNSYN.contains(journalpost.getGjeldendeTema());
		return !isTemaUnntattInnsyn || journalpost.innsynVises();
	}

	/**
	 * 1f) Bruker får ikke se journalposter som er begrenset ihht. GDPR
	 */
	boolean isJournalpostGDPRRestricted(TilgangJournalpost journalpost) {
		return journalpost.getSkjerming().erSkjermet;
	}

	/**
	 * 1g) Hvis journalpost er notat må hoveddokumentet være markert som "forvaltningsnotat" eller
	 * innsyn bør begynne med VISES_* for å vise journalposten.
	 */
	boolean isJournalpostNotatXNORForvaltningsnotat(TilgangJournalpost journalpost) {
		Optional<TilgangDokument> hoveddokument = journalpost.getDokumenter().stream()
				.filter(TilgangDokument::hoveddokument).findFirst();
		if (TilgangJournalposttype.NOTAT == journalpost.getJournalposttype() && hoveddokument.isPresent()) {
			boolean isForvaltningsnotat = FORVALTNINGSNOTAT.equals(hoveddokument.get().kategori());
			return isForvaltningsnotat || journalpost.innsynVises();
		}
		return true;
	}

	/**
	 * 1i) Bruker kan ikke få se journalposter der innsyn begynner med SKJULES_*
	 */
	boolean isJournalpostInnsynSkjules(TilgangJournalpost journalpost) {
		return journalpost.innsynSkjules();
	}

	/**
	 * 2a) Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises
	 */
	boolean isAvsenderMottakerPart(TilgangJournalpost tilgangJournalpost, Set<Ident> idents) {
		final Ident avsenderMottakerId = tilgangJournalpost.getAvsenderMottakerId();
		// Notat er unntatt
		if (TilgangJournalposttype.NOTAT == tilgangJournalpost.getJournalposttype()) {
			return true;
		}
		if (avsenderMottakerId == null) {
			return false;
		}

		return idents.contains(avsenderMottakerId) || tilgangJournalpost.innsynVises();
	}

	/**
	 * 2b) Bruker får ikke se skannede dokumenter med mindre innsyn begynner med VISES_*
	 */
	boolean isSkannetDokumentAndInnsynIsNotVises(TilgangJournalpost tilgangJournalpost) {
		return tilgangJournalpost.getMottakskanal() == TilgangMottakskanal.SKANNING && !tilgangJournalpost.innsynVises();
	}

	/**
	 * 2e) Dokumentvariant som er begrenset ihht. GDPR skal ikke vises
	 */
	boolean isDokumentvariantGDPRRestricted(TilgangVariant tilgangVariant) {
		if (tilgangVariant == null) {
			return false;
		}
		return tilgangVariant.skjerming().erSkjermet;
	}

	/**
	 * 2e) Dokumenter som er begrenset ihht. GDPR skal ikke vises
	 */
	boolean isDokumentGDPRRestricted(TilgangDokument tilgangDokument) {
		if (tilgangDokument == null) {
			return false;
		}
		return tilgangDokument.skjerming().erSkjermet;
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	boolean isDokumentKassert(TilgangDokument tilgangDokument) {
		if (tilgangDokument == null) {
			return false;
		}
		return tilgangDokument.kassert();
	}

	public static boolean isBlank(String string) {
		return string == null || string.isBlank();
	}
}
