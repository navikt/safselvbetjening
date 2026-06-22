package no.nav.safselvbetjening.representasjon.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record FullmaktsforholdDto(
		@JsonProperty("fullmaktsgiver") String fullmaktsgiver,
		@JsonProperty("fullmektig") String fullmektig,
		@JsonProperty("leserettigheter") Set<String> tema
) {
}
