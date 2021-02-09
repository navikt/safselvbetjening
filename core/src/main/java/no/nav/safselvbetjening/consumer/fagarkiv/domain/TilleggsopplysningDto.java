package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TilleggsopplysningDto {
	private String nokkel;
	private String verdi;
}
