package no.nav.safselvbetjening.fullmektig;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record FullmektigTema(@JsonProperty("fullmaktsgiver") String fullmaktsgiver,
							 @JsonProperty("omraade") Set<String> omraade) {

}
