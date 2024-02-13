package no.nav.safselvbetjening.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
public class RelevantDato {
	// Fallback for datoer som er påkrevd men av ukjente årsaker ikke finnes.
	public static final LocalDateTime INVALID_DATE = LocalDateTime.of(LocalDate.of(1, 1, 1), LocalTime.of(0, 0));
	public static final ZoneId TIDSSONE_NORGE = ZoneId.of("Europe/Oslo");

	private final LocalDateTime dato;
	private final Datotype datotype;

	@JsonCreator
	public RelevantDato(@JsonProperty("dato") LocalDateTime dato, @JsonProperty("datotype") Datotype datotype) {
		this.dato = dato;
		this.datotype = datotype;
	}

	public RelevantDato(OffsetDateTime dato, Datotype datotype) {
		this.dato = toLocalDateTime(dato);
		this.datotype = datotype;
	}

	public RelevantDato(Date dato, Datotype datotype) {
		this.dato = toLocalDateTime(dato);
		this.datotype = datotype;
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return INVALID_DATE;
		}
		return LocalDateTime.from(date.toInstant().atZone(TIDSSONE_NORGE));
	}

	private static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
		if (offsetDateTime == null) {
			return INVALID_DATE;
		}
		return offsetDateTime.atZoneSameInstant(TIDSSONE_NORGE).toLocalDateTime();
	}
}
