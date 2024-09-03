package no.nav.safselvbetjening.consumer.sak;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@JsonDeserialize(builder = Joarksak.JoarksakBuilder.class)
@Value
@Builder
public class Joarksak {
	Long id;
	String tema;
	String applikasjon;
	String aktoerId;
	String orgnr;
	String fagsakNr;
	String opprettetAv;
	OffsetDateTime opprettetTidspunkt;

	@JsonPOJOBuilder(withPrefix = "")
	public static class JoarksakBuilder {

	}
}
