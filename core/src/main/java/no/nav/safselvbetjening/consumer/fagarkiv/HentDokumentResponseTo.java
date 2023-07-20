package no.nav.safselvbetjening.consumer.fagarkiv;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

@Data
@Builder
public class HentDokumentResponseTo {
	private final byte[] dokument;
	private final MediaType mediaType;
}
