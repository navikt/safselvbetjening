package no.nav.safselvbetjening.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Sak {
	private final String fagsakId;
	private final String fagsaksystem;
	private final Sakstype sakstype;

	@JsonIgnore
	public boolean isFagsak() {
		return FAGSAK == sakstype;
	}
}
