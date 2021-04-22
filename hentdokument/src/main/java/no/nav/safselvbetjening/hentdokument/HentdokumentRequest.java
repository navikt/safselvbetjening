package no.nav.safselvbetjening.hentdokument;

import lombok.Builder;
import lombok.Value;
import no.nav.security.token.support.core.context.TokenValidationContext;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class HentdokumentRequest {
	private final String journalpostId;
	private final String dokumentInfoId;
	private final String variantFormat;
	private final TokenValidationContext tokenValidationContext;
}
