package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_IM;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_NETS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.FEIL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode.POL;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KASSERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.SKANNET_DOKUMENT;

/**
 * Regler for tilgangskontroll for journalposter: https://confluence.adeo.no/pages/viewpage.action?pageId=377182021
 */
@Component
public class UtledTilgangDokumentService {

	private static final EnumSet<MottaksKanalCode> ACCAPTED_MOTTAKS_KANAL = EnumSet.of(SKAN_IM, SKAN_NETS, SKAN_PEN);
	private static final EnumSet<SkjermingTypeCode> GDPR_SKJERMING_TYPE = EnumSet.of(POL, FEIL);

	public List<String> utledTilgangDokument(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto, BrukerIdenter brukerIdenter) {
		List<String> feilmeldinger = new ArrayList<>();

		if (!isAvsenderMottakerPart(journalpostDto, brukerIdenter.getIdenter())) {
			feilmeldinger.add(PARTSINNSYN);
		}
		if (isSkannetDokument(journalpostDto)) {
			feilmeldinger.add(SKANNET_DOKUMENT);
		}
		if (isDokumentInnskrenketPartsinnsyn(dokumentInfoDto)) {
			feilmeldinger.add(INNSKRENKET_PARTSINNSYN);
		}
		if (isDokumentGDPRRestricted(dokumentInfoDto)) {
			feilmeldinger.add(GDPR);
		}
		if (isDokumentKassert(dokumentInfoDto)) {
			feilmeldinger.add(KASSERT);
		}

		return feilmeldinger;
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
		return ACCAPTED_MOTTAKS_KANAL.contains(journalpostDto.getMottakskanal());
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
	private boolean isDokumentGDPRRestricted(DokumentInfoDto dokumentInfoDto) {
		return dokumentInfoDto.getVarianter().stream().anyMatch(variantDto -> GDPR_SKJERMING_TYPE.contains(variantDto.getSkjerming()));
	}

	/**
	 * 2f) Kasserte dokumenter skal ikke vises
	 */
	private boolean isDokumentKassert(DokumentInfoDto dokumentInfoDto) {
		return dokumentInfoDto.getKassert() != null && dokumentInfoDto.getKassert();
	}
}
