package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class Fagsak {
	@Builder.Default
	@EqualsAndHashCode.Exclude
	List<Journalpost> journalposter = new ArrayList<>();
	String fagsakId;
	String fagsaksystem;
	String tema;
}
