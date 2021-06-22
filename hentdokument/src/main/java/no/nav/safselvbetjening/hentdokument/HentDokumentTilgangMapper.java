package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangBrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangDokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangSakDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangVariantDto;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class HentDokumentTilgangMapper {

	public Journalpost map(TilgangJournalpostDto tilgangJournalpostDto, BrukerIdenter brukerIdenter) {
		return Journalpost.builder()
				.journalpostId(tilgangJournalpostDto.getJournalpostId())
				.journalposttype(tilgangJournalpostDto.getJournalpostType() == null ? null : tilgangJournalpostDto.getJournalpostType().toSafJournalposttype())
				.journalstatus(tilgangJournalpostDto.getJournalStatus() == null ? null : tilgangJournalpostDto.getJournalStatus().toSafJournalstatus())
				.kanal(mapKanal(tilgangJournalpostDto))
				.tilgang(mapJournalpostTilgang(tilgangJournalpostDto, brukerIdenter))
				.dokumenter(Collections.singletonList(mapDokumenter(tilgangJournalpostDto.getDokument())))
				.build();
	}

	private DokumentInfo mapDokumenter(TilgangDokumentInfoDto tilgangDokumentInfoDto) {
		if (tilgangDokumentInfoDto == null) {
			return null;
		}
		return DokumentInfo.builder()
				.tilgangDokument(DokumentInfo.TilgangDokument.builder()
						.innskrenketPartsinnsyn(tilgangDokumentInfoDto.getInnskrenketPartsinnsyn() != null && tilgangDokumentInfoDto.getInnskrenketPartsinnsyn())
						.innskrenketTredjepart(tilgangDokumentInfoDto.getInnskrenketTredjepart() != null && tilgangDokumentInfoDto.getInnskrenketTredjepart())
						.kassert(tilgangDokumentInfoDto.getKassert() != null && tilgangDokumentInfoDto.getKassert())
						.kategori(tilgangDokumentInfoDto.getKategori())
						.organinternt(tilgangDokumentInfoDto.getOrganinternt() != null && tilgangDokumentInfoDto.getOrganinternt())
						.build())
				.dokumentvarianter(Collections.singletonList(mapDokumentVarianter(tilgangDokumentInfoDto.getVariant())))
				.build();
	}

	private Dokumentvariant mapDokumentVarianter(TilgangVariantDto tilgangVariantDto) {

		return Dokumentvariant.builder()
				.tilgangVariant(Dokumentvariant.TilgangVariant.builder()
						.skjerming(mapSkjermingType(tilgangVariantDto.getSkjerming()))
						.build())
				.build();
	}

	private Journalpost.TilgangJournalpost mapJournalpostTilgang(TilgangJournalpostDto tilgangJournalpostDto, BrukerIdenter brukerIdenter) {
		return Journalpost.TilgangJournalpost.builder()
				.datoOpprettet(tilgangJournalpostDto.getDatoOpprettet())
				.mottakskanal(mapTilgangMottakskanal(tilgangJournalpostDto.getMottakskanal()))
				.tema(tilgangJournalpostDto.getFagomrade() == null ? null : tilgangJournalpostDto.getFagomrade().name())
				.avsenderMottakerId(tilgangJournalpostDto.getAvsenderMottakerId())
				.journalfoertDato(tilgangJournalpostDto.getJournalfoertDato())
				.skjerming(mapSkjermingType(tilgangJournalpostDto.getSkjerming()))
				.tilgangBruker(mapTilgangBruker(tilgangJournalpostDto.getBruker()))
				.tilgangSak(mapTilgangSak(tilgangJournalpostDto.getSak(), brukerIdenter))
				.build();
	}

	private Kanal mapTilgangMottakskanal(MottaksKanalCode mottakskanal) {
		if(mottakskanal == null) {
			return null;
		}
		return mottakskanal.getSafKanal();
	}

	private Journalpost.TilgangBruker mapTilgangBruker(TilgangBrukerDto tilgangBrukerDto) {
		if (tilgangBrukerDto == null) {
			return null;
		}
		return Journalpost.TilgangBruker.builder().brukerId(tilgangBrukerDto.getBrukerId()).build();
	}

	private Journalpost.TilgangSak mapTilgangSak(TilgangSakDto tilgangSakDto, BrukerIdenter brukerIdenter) {
		if (tilgangSakDto == null) {
			return null;
		}

		return Journalpost.TilgangSak.builder()
				.aktoerId(tilgangSakDto.getAktoerId())
				.foedselsnummer(FagsystemCode.PEN.name().equals(tilgangSakDto.getFagsystem()) ? brukerIdenter.getFoedselsnummer().get(0) : null)
				.fagsystem(tilgangSakDto.getFagsystem())
				.feilregistrert(tilgangSakDto.getFeilregistrert() != null && tilgangSakDto.getFeilregistrert())
				.tema(tilgangSakDto.getTema())
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

	private Kanal mapKanal(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getJournalpostType() == null) {
			return null;
		}

		switch (tilgangJournalpostDto.getJournalpostType()) {
			case I:
				if (tilgangJournalpostDto.getMottakskanal() == null) {
					return Kanal.UKJENT;
				}
				return tilgangJournalpostDto.getMottakskanal().getSafKanal();
			case U:
				// utsendingskanal returneres ikke fra grensesnitt. Dette er en workaround for lokal utskrift
				// Dvs brevet er printet ut av saksbehandler lokalt og skannet inn hos skanning leverandør.
				if (tilgangJournalpostDto.getMottakskanal() == null) {
					return mapManglendeUtsendingskanal(tilgangJournalpostDto);
				}
				return tilgangJournalpostDto.getMottakskanal().getSafKanal();
			case N:
				return Kanal.INGEN_DISTRIBUSJON;
			default:
				return null;
		}
	}

	private Kanal mapManglendeUtsendingskanal(TilgangJournalpostDto tilgangJournalpostDto) {
		switch (tilgangJournalpostDto.getJournalStatus()) {
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
}
