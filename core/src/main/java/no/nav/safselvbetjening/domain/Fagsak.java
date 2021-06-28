package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder(toBuilder = true)
public class Fagsak {
	@Builder.Default
	@EqualsAndHashCode.Exclude
	private final List<Journalpost> journalposter = new ArrayList<>();
	private final String fagsakId;
	private final String fagsaksystem;
	private final String tema;
}
