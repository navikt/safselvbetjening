package no.nav.safselvbetjening.tilgang;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Date.from;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.N;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_IM;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_NETS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.FEIL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.POL;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KASSERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.SKANNET_DOKUMENT;

/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
@Slf4j
@Component
public class UtledTilgangDokumentoversiktService {

	private static final EnumSet<MottaksKanalCode> ACCEPTED_MOTTAKS_KANAL = EnumSet.of(SKAN_IM, SKAN_NETS, SKAN_PEN);
	private static final EnumSet<SkjermingTypeCode> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);

	private final Date tidligstInnsynDato;

	public UtledTilgangDokumentoversiktService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.tidligstInnsynDato = from(safSelvbetjeningProperties.getTidligstInnsynDato().atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public List<JournalpostDto> utledTilgangJournalposter(List<JournalpostDto> journalpostDtoList, BrukerIdenter identer) {

		return journalpostDtoList.stream()
				.filter(journalpostDto -> isBrukerPart(journalpostDto, identer))
				.filter(this::isNotJournalpostGDPRRestricted)
				.filter(this::isNotJournalpostKontrollsak)
				.filter(this::isJournalpostForvaltningsnotat)
				.filter(this::isNotJournalpostOrganInternt)
				.collect(Collectors.toList());
	}

	public List<String> utledTilgangDokument(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto, BrukerIdenter brukerIdenter, VariantDto variantDto) {
		List<String> feilmeldinger = new ArrayList<>();

		if (!isAvsenderMottakerPart(journalpostDto, brukerIdenter.getIdenter())) {
			feilmeldinger.add(PARTSINNSYN);
		}
		if (!isJournalfoertDatoAfterInnsynsdato(journalpostDto)) {
			feilmeldinger.add(INNSYNSDATO);
		}
		if (isSkannetDokument(journalpostDto)) {
			feilmeldinger.add(SKANNET_DOKUMENT);
		}
		if (isDokumentInnskrenketPartsinnsyn(dokumentInfoDto)) {
			feilmeldinger.add(INNSKRENKET_PARTSINNSYN);
		}
		if (isDokumentGDPRRestricted(variantDto)) {
			feilmeldinger.add(GDPR);
		}
		if (isDokumentKassert(dokumentInfoDto)) {
			feilmeldinger.add(KASSERT);
		}

		return feilmeldinger;
	}

	/**
	 * 1a) Bruker må være part for å se journalposter
	 */

	//Todo: Kan denne bare fjernes?
	private boolean isBrukerPart(JournalpostDto journalpostDto, BrukerIdenter identer) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (JournalStatusCode.getJournalstatusMidlertidig().contains(journalStatusCode)) {
			return identer.getIdenter().contains(journalpostDto.getBruker().getBrukerId());
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(journalStatusCode)) {
			if (journalpostDto.getSaksrelasjon().getFagsystem() == FS22) {
				return identer.getIdenter().contains(journalpostDto.getSaksrelasjon().getAktoerId());
			} else if (journalpostDto.getSaksrelasjon().getFagsystem() == PEN) {
				return identer.getFoedselsnummer().contains(journalpostDto.getBruker().getBrukerId());
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
	 * 1e) Bruker får ikke innsyn i kontrollsaker
	 */
	private boolean isNotJournalpostKontrollsak(JournalpostDto journalpostDto) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (JournalStatusCode.getJournalstatusMidlertidig().contains(journalStatusCode)) {
			return journalpostDto.getFagomrade() != FagomradeCode.KTR;
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(journalStatusCode) &&
				journalpostDto.getSaksrelasjon() != null) {
			return !Tema.KTR.toString().equals(journalpostDto.getSaksrelasjon().getTema());
		} else if(JournalStatusCode.getJournalstatusFerdigstilt().contains(journalStatusCode) &&
				journalpostDto.getSaksrelasjon() == null) {
			return journalpostDto.getFagomrade() != FagomradeCode.KTR;
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
}
