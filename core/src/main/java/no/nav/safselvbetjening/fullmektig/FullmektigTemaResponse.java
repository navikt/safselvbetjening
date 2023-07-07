package no.nav.safselvbetjening.fullmektig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record FullmektigTemaResponse(@JsonProperty("fullmaktsgiver") String fullmaktsgiver, @JsonProperty("tema") Set<String> tema) {
}
