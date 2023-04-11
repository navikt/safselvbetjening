package no.nav.safselvbetjening.consumer.pensjon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Pensjonsak(
		String sakId,
		String arkivtema
) {

	@JsonCreator
	public Pensjonsak(
			@JsonProperty("sakId") String sakId,
			@JsonProperty("arkivtema") String arkivtema
	) {
		this.sakId = sakId;
		this.arkivtema = arkivtema;
	}
}