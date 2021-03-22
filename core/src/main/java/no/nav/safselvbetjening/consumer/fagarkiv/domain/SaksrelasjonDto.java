package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
@AllArgsConstructor
public class SaksrelasjonDto {
	private String sakId;
	private Boolean feilregistrert;
	private FagsystemCode fagsystem;	//arkivsaksystem - hvor finner du mer informasjon om saken
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String applikasjon;	//fagsaksystem - finnes kun om fagsystem er FS22
	private final String orgnr;
	private final String opprettetAv;
	private final ZonedDateTime opprettetTidspunkt;
}
