package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SaksrelasjonDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.domain.Datotype.DATO_AVS_RETUR;
import static no.nav.safselvbetjening.domain.Datotype.DATO_DOKUMENT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_EKSPEDERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_JOURNALFOERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_OPPRETTET;
import static no.nav.safselvbetjening.domain.Datotype.DATO_REGISTRERT;
import static no.nav.safselvbetjening.domain.Datotype.DATO_SENDT_PRINT;

/**
 * Mapper fra fagarkivet sitt domene til safselvbetjening sitt domene.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class JournalpostMapper {

	private final AvsenderMottakerMapper avsenderMottakerMapper;

	public JournalpostMapper(AvsenderMottakerMapper avsenderMottakerMapper) {
		this.avsenderMottakerMapper = avsenderMottakerMapper;
	}

	Journalpost map(JournalpostDto journalpostDto, BrukerIdenter brukerIdenter) {
		try {
			return Journalpost.builder()
					.journalpostId(journalpostDto.getJournalpostId().toString())
					.journalposttype(journalpostDto.getJournalposttype().toSafJournalposttype())
					.journalstatus(journalpostDto.getJournalstatus().toSafJournalstatus())
					.tittel(journalpostDto.getInnhold())
					.kanal(mapKanal(journalpostDto))
					.avsenderMottaker(avsenderMottakerMapper.map(journalpostDto))
					.relevanteDatoer(mapRelevanteDatoer(journalpostDto))
					.dokumenter(mapDokumenter(journalpostDto))
					.tilgang(mapJournalpostTilgang(journalpostDto, brukerIdenter))
					.build();
		} catch (Exception e) {
			log.error("Teknisk feil under mapping av journalpost med journalpostId={}.", journalpostDto.getJournalpostId(), e);
			return null;
		}
	}

	private Journalpost.TilgangJournalpost mapJournalpostTilgang(JournalpostDto journalpostDto, BrukerIdenter brukerIdenter) {
		String brukerId = journalpostDto.getBruker() == null ? null : journalpostDto.getBruker().getBrukerId();

		return Journalpost.TilgangJournalpost.builder()
				.datoOpprettet(mapDato(journalpostDto.getDatoOpprettet()))
				.fagomradeCode(journalpostDto.getFagomrade().toString())
				.journalfoertDato(mapDato(journalpostDto.getJournalDato()))
				.skjerming(mapSkjermingType(journalpostDto.getSkjerming()))
				.tilgangBruker(Journalpost.TilgangBruker.builder().brukerId(brukerId).build())
				.tilgangSak(mapTilgangSak(journalpostDto.getSaksrelasjon(), brukerIdenter))
				.build();
	}

	private LocalDateTime mapDato(Date dato) {
		if (dato == null) {
			return null;
		}
		return dato.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	private Journalpost.TilgangSak mapTilgangSak(SaksrelasjonDto saksrelasjonDto, BrukerIdenter brukerIdenter) {
		if (saksrelasjonDto == null) {
			return Journalpost.TilgangSak.builder().build();
		}

		return Journalpost.TilgangSak.builder()
				.aktoerId(saksrelasjonDto.getAktoerId())
				.foedselsnummer(FagsystemCode.PEN == saksrelasjonDto.getFagsystem() ? brukerIdenter.getFoedselsnummer().get(0) : null)
				.fagsystem(saksrelasjonDto.getFagsystem() == null ? null : saksrelasjonDto.getFagsystem().toString())
				.feilregistrert(saksrelasjonDto.getFeilregistrert() != null && saksrelasjonDto.getFeilregistrert())
				.tema(saksrelasjonDto.getTema())
				.build();
	}

	private SkjermingType mapSkjermingType(SkjermingTypeCode skjermingTypeCode) {
		if (skjermingTypeCode == null) {
			return null;
		}

		switch (skjermingTypeCode) {
			case POL:
				return SkjermingType.POL;
			case FEIL:
				return SkjermingType.FEIL;
			default:
				return null;
		}
	}

	private List<DokumentInfo> mapDokumenter(JournalpostDto journalpostDto) {
		List<DokumentInfoDto> dokumenter = journalpostDto.getDokumenter();
		return dokumenter.stream().map(dokument -> DokumentInfo.builder()
				.dokumentInfoId(dokument.getDokumentInfoId())
				.dokumentvarianter(mapDokumentVarianter(dokument))
				.tittel(dokument.getTittel())
				.brevkode(dokument.getBrevkode())
				.tilgangDokument(DokumentInfo.TilgangDokument.builder()
						.innskrenketPartsinnsyn(dokument.getInnskrPartsinnsyn() != null && dokument.getInnskrPartsinnsyn())
						.innskrenketTredjepart(dokument.getInnskrTredjepart() != null && dokument.getInnskrTredjepart())
						.kassert(dokument.getKassert() != null && dokument.getKassert())
						.kategori(dokument.getKategori() == null ? null : dokument.getKategori().toString())
						.organinternt(dokument.getOrganInternt() != null && dokument.getOrganInternt())
						.build())
				.build()).collect(Collectors.toList());
	}

	private List<Dokumentvariant> mapDokumentVarianter(DokumentInfoDto dokumentInfoDto) {
		List<VariantDto> varianter = dokumentInfoDto.getVarianter();

		return varianter.stream().map(variantDto -> Dokumentvariant.builder()
				.variantformat(variantDto.getVariantf().getSafVariantformat())
				.filuuid(variantDto.getFiluuid())
				.tilgangVariant(Dokumentvariant.TilgangVariant.builder().skjerming(mapSkjermingType(variantDto.getSkjerming())).build())
				.build()).collect(Collectors.toList());
	}

	private Kanal mapKanal(JournalpostDto journalpostDto) {
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottakskanal() == null) {
					return Kanal.UKJENT;
				}
				return journalpostDto.getMottakskanal().getSafKanal();
			case U:
				if (journalpostDto.getUtsendingskanal() == null) {
					return mapManglendeUtsendingskanal(journalpostDto);
				}
				return journalpostDto.getUtsendingskanal().getSafKanal();
			case N:
				return Kanal.INGEN_DISTRIBUSJON;
			default:
				return null;
		}
	}

	private Kanal mapManglendeUtsendingskanal(JournalpostDto journalpostDto) {
		switch (journalpostDto.getJournalstatus()) {
			case FL:
				return Kanal.LOKAL_UTSKRIFT;
			case FS:
				return Kanal.SENTRAL_UTSKRIFT;
			case E:
				return Kanal.SENTRAL_UTSKRIFT;
			default:
				return null;
		}
	}

	private List<RelevantDato> mapRelevanteDatoer(JournalpostDto journalpostDto) {
		List<RelevantDato> relevanteDatoer = new ArrayList<>();
		relevanteDatoer.add(new RelevantDato(journalpostDto.getDatoOpprettet(), DATO_OPPRETTET));
		if (journalpostDto.getDokumentDato() != null) {
			relevanteDatoer.add(new RelevantDato(journalpostDto.getDokumentDato(), DATO_DOKUMENT));
		}
		if (journalpostDto.getJournalDato() != null) {
			relevanteDatoer.add(new RelevantDato(journalpostDto.getJournalDato(), DATO_JOURNALFOERT));
		}
		switch (journalpostDto.getJournalposttype()) {
			case I:
				if (journalpostDto.getMottattDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getMottattDato(), DATO_REGISTRERT));
				}
				break;
			case U:
				if (journalpostDto.getSendtPrintDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getSendtPrintDato(), DATO_SENDT_PRINT));
				}
				if (journalpostDto.getEkspedertDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getEkspedertDato(), DATO_EKSPEDERT));
				}
				if (journalpostDto.getAvsReturDato() != null) {
					relevanteDatoer.add(new RelevantDato(journalpostDto.getAvsReturDato(), DATO_AVS_RETUR));
				}
				break;
			default:
				return relevanteDatoer;
		}
		return relevanteDatoer;
	}
}
