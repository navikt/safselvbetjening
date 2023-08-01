package no.nav.safselvbetjening.hentdokument;

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

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.domain.Innsyn.valueOf;
import static no.nav.safselvbetjening.domain.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.safselvbetjening.domain.Kanal.LOKAL_UTSKRIFT;
import static no.nav.safselvbetjening.domain.Kanal.SENTRAL_UTSKRIFT;
import static no.nav.safselvbetjening.domain.Kanal.UKJENT;

@Component
class HentDokumentTilgangMapper {

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
						.kassert(tilgangDokumentInfoDto.getKassert() != null && tilgangDokumentInfoDto.getKassert())
						.kategori(tilgangDokumentInfoDto.getKategori())
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
				.journalstatus(tilgangJournalpostDto.getJournalStatus() == null ? null : tilgangJournalpostDto.getJournalStatus().name())
				.datoOpprettet(tilgangJournalpostDto.getDatoOpprettet())
				.mottakskanal(mapTilgangMottakskanal(tilgangJournalpostDto.getMottakskanal()))
				.tema(tilgangJournalpostDto.getFagomrade() == null ? null : tilgangJournalpostDto.getFagomrade().name())
				.avsenderMottakerId(tilgangJournalpostDto.getAvsenderMottakerId())
				.journalfoertDato(tilgangJournalpostDto.getJournalfoertDato())
				.skjerming(mapSkjermingType(tilgangJournalpostDto.getSkjerming()))
				.tilgangBruker(mapTilgangBruker(tilgangJournalpostDto.getBruker()))
				.tilgangSak(mapTilgangSak(tilgangJournalpostDto.getSak(), brukerIdenter))
				.innsyn(tilgangJournalpostDto.getInnsyn() == null ? null : valueOf(tilgangJournalpostDto.getInnsyn().name()))
				.build();
	}

	private Kanal mapTilgangMottakskanal(MottaksKanalCode mottakskanal) {
		if (mottakskanal == null) {
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
				.foedselsnummer(PEN.name().equals(tilgangSakDto.getFagsystem()) ? brukerIdenter.getFoedselsnummer().get(0) : null)
				.fagsystem(tilgangSakDto.getFagsystem())
				.feilregistrert(tilgangSakDto.getFeilregistrert() != null && tilgangSakDto.getFeilregistrert())
				.tema(tilgangSakDto.getTema())
				.build();
	}

	private SkjermingType mapSkjermingType(SkjermingTypeCode skjermingTypeCode) {
		if (skjermingTypeCode == null) {
			return null;
		}

		return switch (skjermingTypeCode) {
			case POL -> SkjermingType.POL;
			case FEIL -> SkjermingType.FEIL;
		};
	}

	private Kanal mapKanal(TilgangJournalpostDto tilgangJournalpostDto) {
		if (tilgangJournalpostDto.getJournalpostType() == null) {
			return null;
		}

		switch (tilgangJournalpostDto.getJournalpostType()) {
			case I -> {
				if (tilgangJournalpostDto.getMottakskanal() == null) {
					return UKJENT;
				}
				return tilgangJournalpostDto.getMottakskanal().getSafKanal();
			}
			case U -> {
				// utsendingskanal returneres ikke fra grensesnitt. Dette er en workaround for lokal utskrift
				// Dvs brevet er printet ut av saksbehandler lokalt og skannet inn hos skanning leverandør.
				if (tilgangJournalpostDto.getMottakskanal() == null) {
					return mapManglendeUtsendingskanal(tilgangJournalpostDto);
				}
				return tilgangJournalpostDto.getMottakskanal().getSafKanal();
			}
			case N -> {
				return INGEN_DISTRIBUSJON;
			}
			default -> {
				return null;
			}
		}
	}

	private Kanal mapManglendeUtsendingskanal(TilgangJournalpostDto tilgangJournalpostDto) {
		return switch (tilgangJournalpostDto.getJournalStatus()) {
			case FL -> LOKAL_UTSKRIFT;
			case FS, E -> SENTRAL_UTSKRIFT;
			default -> null;
		};
	}
}
