package no.nav.safselvbetjening.representasjon.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record VergemaalsforholdDto(
		@JsonProperty("vergehaver") String vergehaver,
		@JsonProperty("verge") String verge,
		@JsonProperty("leserettigheter") Set<String> tema
) {
}
