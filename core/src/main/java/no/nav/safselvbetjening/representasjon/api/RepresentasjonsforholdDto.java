package no.nav.safselvbetjening.representasjon.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RepresentasjonsforholdDto(@JsonProperty("fullmakt") List<FullmaktsforholdDto> fullmakt,
										@JsonProperty("vergemaal") List<VergemaalsforholdDto> vergemaal) {
	public boolean isEmpty() {
		return fullmakt.isEmpty() && vergemaal.isEmpty();
	}

	public static RepresentasjonsforholdDto empty() {
		return new RepresentasjonsforholdDto(List.of(), List.of());
	}
}
