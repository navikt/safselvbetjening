package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangBrukerDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangDokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangSakDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangVariantDto;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangBruker;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangDokument;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangJournalpost;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangSak;
import no.nav.safselvbetjening.tilgang.domain.UtledTilgangVariant;

import java.util.Collections;

public class HentDokumentTilgangMapper {

	public UtledTilgangJournalpost map(TilgangJournalpostDto tilgangJournalpostDto) {
		return UtledTilgangJournalpost.builder()
				.avsenderMottakerId(tilgangJournalpostDto.getAvsenderMottakerId())
				.datoOpprettet(tilgangJournalpostDto.getDatoOpprettet())
				.fagomradeCode(tilgangJournalpostDto.getFagomrade())
				.feilregistrert(tilgangJournalpostDto.getFeilregistrert())
				.journalfoertDato(tilgangJournalpostDto.getJournalfoertDato())
				.journalpostType(tilgangJournalpostDto.getJournalpostType())
				.journalstatusCode(tilgangJournalpostDto.getJournalStatus())
				.mottaksKanalCode(tilgangJournalpostDto.getMottakskanal())
				.skjerming(tilgangJournalpostDto.getSkjerming())
				.utledTilgangBruker(mapBruker(tilgangJournalpostDto.getBruker()))
				.utledTilgangDokumentList(Collections.singletonList(mapDokument(tilgangJournalpostDto.getDokument())))
				.utledTilgangSak(mapSak(tilgangJournalpostDto.getSak()))
				.build();
	}

	private UtledTilgangSak mapSak(TilgangSakDto tilgangSakDto) {
		return UtledTilgangSak.builder()
				.aktoerId(tilgangSakDto.getAktoerId())
				.fagsystem(tilgangSakDto.getFagsystem())
				.tema(tilgangSakDto.getTema())
				.build();
	}

	private UtledTilgangDokument mapDokument(TilgangDokumentInfoDto tilgangDokumentInfoDto) {
		return UtledTilgangDokument.builder()
				.innskrenketPartsinnsyn(tilgangDokumentInfoDto.getInnskrenketPartsinnsyn())
				.innskrenketTredjepart(tilgangDokumentInfoDto.getInnskrenketTredjepart())
				.kassert(tilgangDokumentInfoDto.getKassert())
				.kategori(tilgangDokumentInfoDto.getKategori())
				.organinternt(tilgangDokumentInfoDto.getOrganinternt())
				.variantList(Collections.singletonList(mapVariant(tilgangDokumentInfoDto.getVariant())))
				.build();
	}

	private UtledTilgangVariant mapVariant(TilgangVariantDto tilgangVariantDto) {
		return UtledTilgangVariant.builder().skjerming(tilgangVariantDto.getSkjerming()).build();
	}

	private UtledTilgangBruker mapBruker(TilgangBrukerDto tilgangBrukerDto) {
		return UtledTilgangBruker.builder().brukerId(tilgangBrukerDto.getBrukerId()).build();
	}
}
