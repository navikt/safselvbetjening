package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentKategoriCode;

import java.util.ArrayList;
import java.util.List;


@Value
@ToString(exclude = "parent")
@Builder
public class DokumentInfo {

	private final Journalpost parent;

	private final String dokumentInfoId;
	private final String tittel;
	private final String brevkode;
	private final TilgangDokument tilgangDokument;

	@Builder.Default
	private final List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

	@Data
	@Builder
	public static class TilgangDokument {
		private final String kategori;
		private final boolean organinternt;
		private final boolean innskrenketPartsinnsyn;
		private final boolean innskrenketTredjepart;
		private final boolean kassert;
	}
}
