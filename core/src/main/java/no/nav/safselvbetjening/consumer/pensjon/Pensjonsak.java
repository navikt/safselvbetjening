package no.nav.safselvbetjening.consumer.pensjon;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Pensjonsak {
	String sakNr;
	String tema;
	LocalDateTime datoOpprettet;
}
