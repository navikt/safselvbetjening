package no.nav.safselvbetjening.consumer.dokarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
public class SaksrelasjonDto {
	private String sakId;
	private Boolean feilregistrert;
	private FagsystemCode fagsystem;
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String applikasjon;
	private final String orgnr;
	private final String opprettetAv;
	private final ZonedDateTime opprettetTidspunkt;
}
