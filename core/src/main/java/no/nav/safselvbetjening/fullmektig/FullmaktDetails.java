package no.nav.safselvbetjening.fullmektig;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;

public record FullmaktDetails(@JsonProperty("fullmaktsgiver") String fullmaktsgiver,
							  @JsonProperty("fullmektig") String fullmektig,
							  @JsonProperty("omraade") String omraade,
							  @JsonProperty("gyldigFraOgMed") @JsonSerialize(using = LocalDateSerializer.class) LocalDate gyldigFraOgMed,
							  @JsonProperty("gyldigTilOgMed") @JsonSerialize(using = LocalDateSerializer.class) LocalDate gyldigTilOgMed) {

}
