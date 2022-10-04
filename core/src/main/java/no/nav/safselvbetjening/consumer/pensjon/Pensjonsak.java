package no.nav.safselvbetjening.consumer.pensjon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public record Pensjonsak(
		String sakNr,
		String tema
) {

	@JsonCreator
	public Pensjonsak(
			@JsonProperty("sakId") String sakNr,
			@JsonProperty("arkivtema") String tema
	) {
		this.sakNr = sakNr;
		this.tema = tema;
	}
}