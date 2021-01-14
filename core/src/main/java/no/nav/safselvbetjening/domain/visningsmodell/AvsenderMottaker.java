package no.nav.safselvbetjening.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AvsenderMottaker {
	private final String id;
	private final AvsenderMottakerIdType type;
}
