package no.nav.safselvbetjening.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.domain.kode.Variantformat;


@Value
@Builder
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final boolean brukerHarTilgang;
	private final String code;
}
