package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TilleggsopplysningDto {
	private String nokkel;
	private String verdi;
}
