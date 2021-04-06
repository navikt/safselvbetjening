package no.nav.safselvbetjening.tilgang.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode;

import java.util.List;

@Value
@Builder
public class UtledTilgangDokument {
	private final DokumentKategoriCode kategori;
	private final boolean organinternt;
	private final boolean innskrenketPartsinnsyn;
	private final boolean innskrenketTredjepart;
	private final boolean kassert;
	private final List<UtledTilgangVariant> variantList;
}
