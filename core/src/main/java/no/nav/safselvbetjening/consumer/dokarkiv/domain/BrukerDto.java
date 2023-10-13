package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrukerDto {
	private String brukerId;
	private String brukerIdType;
}
