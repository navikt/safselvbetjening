package no.nav.safselvbetjening.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class RelevantDato {
	// Fallback for datoer som er påkrevd men av ukjente årsaker ikke finnes.
	public static final LocalDateTime INVALID_DATE = LocalDateTime.of(LocalDate.of(1, 1, 1), LocalTime.of(0, 0));

	private final LocalDateTime dato;
	private final Datotype datotype;

	@JsonCreator
	public RelevantDato(@JsonProperty("dato") LocalDateTime dato, @JsonProperty("datotype") Datotype datotype) {
		this.dato = dato;
		this.datotype = datotype;
	}

	@JsonCreator
	public RelevantDato(@JsonProperty("dato") Date dato, @JsonProperty("datotype") Datotype datotype) {
		this.dato = toLocalDateTime(dato);
		this.datotype = datotype;
	}

	private static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return INVALID_DATE;
		}
		return LocalDateTime.from(date.toInstant().atZone(ZoneId.systemDefault()));
	}
}
