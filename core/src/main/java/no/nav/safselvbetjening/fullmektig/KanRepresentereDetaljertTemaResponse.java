package no.nav.safselvbetjening.fullmektig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record KanRepresentereDetaljertTemaResponse(@JsonProperty("fullmaktsgiver") String fullmaktsgiver, @JsonProperty("fullmektig") String fullmektig, @JsonProperty("leserettigheter") Set<String> tema) {
}
