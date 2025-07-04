package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

import java.util.List;

public record ArkivDokumentinfo(Long dokumentInfoId,
								String tilknyttetSom,
								String skjerming,
								String kategori,
								Boolean kassert,
								Integer rekkefoelge,
								List<ArkivFildetaljer> fildetaljer,
								// kun metadata, ikke brukt til tilgangskontroll
								String tittel,
								String brevkode,
								Boolean sensitivt
) {

	public TilgangDokument getTilgangDokument() {
		return TilgangDokument.builder()
				.id(dokumentInfoId)
				.kassert(kassert != null && kassert)
				.kategori(kategori)
				.hoveddokument(ArkivJournalpostMapper.TILKNYTTET_SOM_HOVEDDOKUMENT.equals(tilknyttetSom))
				.skjerming(TilgangSkjermingType.from(skjerming))
				.dokumentvarianter(fildetaljer.stream().map(ArkivFildetaljer::getTilgangVariant).toList())
				.build();
	}
}
