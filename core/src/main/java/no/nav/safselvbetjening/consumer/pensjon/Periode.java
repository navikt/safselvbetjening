package no.nav.safselvbetjening.consumer.pensjon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record Periode(
		LocalDate fom
) {
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_DATE;

	private static LocalDate nullsafeParseDate(String dateString) {
		return dateString != null ? LocalDate.from(dateFormat.parse(dateString)) : null;
	}
}