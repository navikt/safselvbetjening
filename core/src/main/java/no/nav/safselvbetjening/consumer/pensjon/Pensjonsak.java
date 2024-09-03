package no.nav.safselvbetjening.consumer.pensjon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.safselvbetjening.domain.Tema;

public record Pensjonsak(
		Long sakId,
		String arkivtema
) {

	@JsonCreator
	public Pensjonsak(
			@JsonProperty("sakId") Long sakId,
			@JsonProperty("arkivtema") String arkivtema
	) {
		this.sakId = sakId;
		this.arkivtema = arkivtema;
	}

	@Override
	public String arkivtema() {
		if (arkivtema == null) {
			return Tema.PEN.name();
		}
		return arkivtema;
	}
}