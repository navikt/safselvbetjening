package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
public class AvsenderMottaker {
	@ToString.Exclude
	private final String id;
	private final AvsenderMottakerIdType type;
}
