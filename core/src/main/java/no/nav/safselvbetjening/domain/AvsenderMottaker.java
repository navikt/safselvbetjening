package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AvsenderMottaker {
	private final String id;
	private final AvsenderMottakerIdType type;
}
