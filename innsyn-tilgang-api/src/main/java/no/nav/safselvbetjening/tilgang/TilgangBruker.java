package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
public class TilgangBruker {
	@ToString.Exclude
	private final String brukerId;
}
