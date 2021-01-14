package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

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

	@Builder.Default
	private final List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
}
