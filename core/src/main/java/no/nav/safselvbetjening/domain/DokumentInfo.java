package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Value
@Builder
public class DokumentInfo {
	@ToString.Exclude
	Journalpost parent;

	String dokumentInfoId;
	@ToString.Exclude
	String tittel;
	String brevkode;
	Boolean sensitivtPselv;
	TilgangDokument tilgangDokument;

	@Builder.Default
	List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

	@Data
	@Builder
	public static class TilgangDokument {
		private final String kategori;
		private final boolean kassert;
	}
}
