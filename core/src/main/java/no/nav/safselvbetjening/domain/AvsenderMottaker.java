package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
public class AvsenderMottaker {
	@ToString.Exclude
	String id;
	AvsenderMottakerIdType type;
}
