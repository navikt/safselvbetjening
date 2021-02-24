package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Tema;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_IM;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_NETS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode.SKAN_PEN;

@Component
public class UtledTilgangService {

	public UtledTilgangService() {
	}

	public List<JournalpostDto> utledTilgangJournalpost(List<JournalpostDto> journalpostDtoList, List<String> identer) {

		List<JournalpostDto> journalpostDtoList1 = journalpostDtoList.stream()
				.filter(journalpostDto -> isBrukerPart(journalpostDto, identer))
				.filter(journalpostDto -> isJournalpostFerdigstiltOrMidlertidig(journalpostDto.getJournalstatus()))
				.filter(journalpostDto -> !isJournalpostKontrollsak(journalpostDto))
				.filter(journalpostDto -> !isJournalpostGDPRRestricted(journalpostDto.getSkjerming()))
				.filter(journalpostDto -> !isJournalpostForvantningsnotat(journalpostDto))
				.filter(journalpostDto -> !isJournalpostOrganInternt(journalpostDto))
				.collect(Collectors.toList());

		//todo: utledTilgangDokumenter(journalpostDto)

		return journalpostDtoList1;
	}

	private boolean isBrukerPart(JournalpostDto journalpostDto, List<String> aktoerIds) {

		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (journalStatusCode == M || journalStatusCode == MO) {
			return aktoerIds.contains(journalpostDto.getBruker().getBrukerId());
		} else if (journalStatusCode == FS || journalStatusCode == FL || journalStatusCode == J || journalStatusCode == E) {
			if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.FS22) {
				return aktoerIds.contains(journalpostDto.getSaksrelasjon().getAktoerId());
			} else if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.PEN) {
				//todo: Sjekk brukers identer mot PSAK-ident (hva er PSAK-ident?) Riktig?
				return aktoerIds.contains(journalpostDto.getBruker().getBrukerId());
			}
		}
		return false;
	}

	private boolean isJournalpostFerdigstiltOrMidlertidig(JournalStatusCode journalStatusCode) {
		return journalStatusCode == M || journalStatusCode == MO || journalStatusCode == J || journalStatusCode == FS
				|| journalStatusCode == FL || journalStatusCode == E;
	}

	private boolean isJournalpostKontrollsak(JournalpostDto journalpostDto) {
		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (journalStatusCode == M || journalStatusCode == MO) {
			return journalpostDto.getFagomrade() == FagomradeCode.KTR;
		} else if (journalStatusCode == FL || journalStatusCode == J || journalStatusCode == FS || journalStatusCode == E) {
			return journalpostDto.getSaksrelasjon().getTema().equals(Tema.KTR.toString());
		}
		return false;
	}

	private boolean isJournalpostGDPRRestricted(SkjermingTypeCode skjermingTypeCode){
		return skjermingTypeCode == SkjermingTypeCode.POL || skjermingTypeCode == SkjermingTypeCode.FEIL;
	}

	private boolean isJournalpostForvantningsnotat(JournalpostDto journalpostDto){
		if(journalpostDto.getJournalposttype() == JournalpostTypeCode.N){
			//sjekkes om noen av underliggende dokumenter har dokumentkategori "forvaltningsnotat"
			//Isåfall journalpost + dokumenter ikke vises
			//NB: Alle notater har kun ett dokument, så dette kan enten implementeres som en "sjekk alle underliggende dokumenter" eller "sjekk hoveddokument"-regel - gjør det enkleste.
			return true;
		}
		return false;
	}

	private boolean isJournalpostOrganInternt(JournalpostDto journalpostDto){

		//Det må sjekkes at ingen av journalpostenes underliggende dokumenter er markert som "organinterne".
		// Om noen av dokumentene har denne markeringen, skal tilgang til hele journalposten avvises.
		return false;
	}

	private List<DokumentInfoDto> utledTilgangDokumenter(JournalpostDto journalpostDto, List<String> idents){
		List<DokumentInfoDto> dokumentInfoDtoList = journalpostDto.getDokumenter();
		return dokumentInfoDtoList.stream()
				.filter(dokumentInfoDto -> isAvsenderMotakerPart(journalpostDto, idents))
				.filter(dokumentInfoDto -> !isSkannetDokument(journalpostDto))
				.filter(dokumentInfoDto -> isDokumentInnskrenketPartsinnsyn(journalpostDto))
				.filter(dokumentInfoDto -> isDokumentGDPRRestricted(dokumentInfoDto))
				.filter(dokumentInfoDto -> isDokumentKassert(dokumentInfoDto))
				.collect(Collectors.toList());
	}

	private boolean isAvsenderMotakerPart(JournalpostDto journalpostDto, List<String> idents){
		if(journalpostDto.getJournalposttype() != JournalpostTypeCode.N){
			return idents.contains(journalpostDto.getAvsenderMottakerId());
		}
		return true;
	}

	private boolean isSkannetDokument(JournalpostDto journalpostDto){
		MottaksKanalCode mottaksKanalCode = journalpostDto.getMottakskanal();
		return mottaksKanalCode == SKAN_IM || mottaksKanalCode == SKAN_NETS || mottaksKanalCode == SKAN_PEN;
	}

	private boolean isDokumentInnskrenketPartsinnsyn(JournalpostDto journalpostDto){
		//Dersom innskr_partsinnsyn eller innskr_partsinnsyn_tredjepart er satt på dokumentet, skal det ikke vises.
		return false;
	}

	private boolean isDokumentGDPRRestricted(DokumentInfoDto dokumentInfoDto){
		return dokumentInfoDto.getVarianter().stream().anyMatch(variantDto -> (variantDto.getSkjerming() == SkjermingTypeCode.POL || variantDto.getSkjerming() == SkjermingTypeCode.FEIL));
	}

	private boolean isDokumentKassert(DokumentInfoDto dokumentInfoDto){
		return dokumentInfoDto.getKassert();
	}
}
