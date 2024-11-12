package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.nav.safselvbetjening.tilgang.TilgangDokument;

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
	boolean hoveddokument;
	TilgangDokument tilgangDokument;

	@Builder.Default
	List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
}
