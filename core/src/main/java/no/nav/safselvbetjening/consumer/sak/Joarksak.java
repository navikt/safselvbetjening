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
	private final Integer id;
	private final String tema;
	private final String applikasjon;
	private final String aktoerId;
	private final String orgnr;
	private final String fagsakNr;
	private final String opprettetAv;
	private final OffsetDateTime opprettetTidspunkt;

	@JsonPOJOBuilder(withPrefix = "")
	public static class JoarksakBuilder {

	}
}
