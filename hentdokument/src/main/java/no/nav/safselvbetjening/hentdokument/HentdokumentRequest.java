package no.nav.safselvbetjening.hentdokument;

import lombok.Builder;
import lombok.Value;
import no.nav.security.token.support.core.context.TokenValidationContext;

@Value
@Builder
public class HentdokumentRequest {
	String journalpostId;
	String dokumentInfoId;
	String variantFormat;
	TokenValidationContext tokenValidationContext;
}
