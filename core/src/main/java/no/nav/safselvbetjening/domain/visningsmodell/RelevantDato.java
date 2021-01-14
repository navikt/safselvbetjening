package no.nav.safselvbetjening.domain.visningsmodell;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@Data
public class RelevantDato {
	// Fallback for datoer som er påkrevd men av ukjente årsaker ikke finnes.
	public static final LocalDateTime INVALID_DATE = LocalDateTime.of(LocalDate.of(1, 1, 1), LocalTime.of(0, 0));

	private final LocalDateTime datoOpprettet;
	private final LocalDateTime datoMottatt;
	private final LocalDateTime datoEkspedert;


	@JsonCreator
	public RelevantDato(@JsonProperty("dato") Date datoOpprettet, @JsonProperty("dato") Date datoMottatt,
						@JsonProperty("dato") Date datoEkspedert) {
		this.datoOpprettet = toLocalDateTime(datoOpprettet);
		this.datoMottatt = toLocalDateTime(datoMottatt);
		this.datoEkspedert = toLocalDateTime(datoEkspedert);
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return INVALID_DATE;
		}
		return LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault()));
	}
}
