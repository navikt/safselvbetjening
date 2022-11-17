package no.nav.safselvbetjening.hentdokument;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.MediaType;

@Value
@Builder
public class HentDokument {
	byte[] dokument;
	MediaType mediaType;
	String extension;
}
