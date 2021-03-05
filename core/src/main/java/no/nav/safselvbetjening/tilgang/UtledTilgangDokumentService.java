package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_IM;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_NETS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_PEN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.GDPR;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INNSKRENKET_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.KASSERT;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.SKANNET_DOKUMENT;

@Component
public class UtledTilgangDokumentService {

	private final IdentService identService;


	public UtledTilgangDokumentService(IdentService identService) {
		this.identService = identService;
	}

	public List<String> utledTilgangDokument(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto) {
		List<String> feilmeldinger = new ArrayList<>();

		//Todo: Blir dette riktige identer?
		BrukerIdenter brukerIdenter = identService.hentIdenter(journalpostDto.getBruker().getBrukerId());

		if (!isAvsenderMotakerPart(journalpostDto, brukerIdenter.getIdenter())) {
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

	/*public List<DokumentInfoDto> utledTilgangDokumenter(JournalpostDto journalpostDto, List<String> idents) {
		List<DokumentInfoDto> dokumentInfoDtoList = journalpostDto.getDokumenter();
		return dokumentInfoDtoList.stream()
				.filter(dokumentInfoDto -> isAvsenderMotakerPart(journalpostDto, idents))
				.filter(dokumentInfoDto -> !isSkannetDokument(journalpostDto))
				.filter(this::isDokumentInnskrenketPartsinnsyn)
				.filter(this::isDokumentGDPRRestricted)
				.filter(this::isDokumentKassert)
				.collect(Collectors.toList());
	}*/

	private boolean isAvsenderMotakerPart(JournalpostDto journalpostDto, List<String> idents) {
		if (journalpostDto.getJournalposttype() != JournalpostTypeCode.N) {
			return idents.contains(journalpostDto.getAvsenderMottakerId());
		}
		return true;
	}

	private boolean isSkannetDokument(JournalpostDto journalpostDto) {
		MottaksKanalCode mottaksKanalCode = journalpostDto.getMottakskanal();
		return mottaksKanalCode == SKAN_IM || mottaksKanalCode == SKAN_NETS || mottaksKanalCode == SKAN_PEN;
	}

	private boolean isDokumentInnskrenketPartsinnsyn(DokumentInfoDto dokumentInfoDto) {
		return dokumentInfoDto.getInnskrPartsinnsyn();
		//todo: Mangler innskr_partsinnsyn_tredjepart
		//Dersom innskr_partsinnsyn eller innskr_partsinnsyn_tredjepart er satt på dokumentet, skal det ikke vises.
	}

	private boolean isDokumentGDPRRestricted(DokumentInfoDto dokumentInfoDto) {
		return dokumentInfoDto.getVarianter().stream().anyMatch(variantDto -> (variantDto.getSkjerming() == SkjermingTypeCode.POL || variantDto.getSkjerming() == SkjermingTypeCode.FEIL));
	}

	private boolean isDokumentKassert(DokumentInfoDto dokumentInfoDto) {
		return dokumentInfoDto.getKassert();
	}
}
