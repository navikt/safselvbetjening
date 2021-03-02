package no.nav.safselvbetjening.hentdokument;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.MediaType;

@Value
@Builder
public class HentDokument {
	private final byte[] dokument;
	private final MediaType mediaType;
	private final String extension;
}
