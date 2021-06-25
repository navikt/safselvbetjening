package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Sak {
	private final String fagsakId;
	private final String fagsaksystem;
	private final Sakstype sakstype;
}
