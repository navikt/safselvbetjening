package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangSakDto {
	private String sakId;
	private String fagsystem;
	private Boolean feilregistrert;
	private String aktoerId;
	private String tema;
	private String fagsakNr;
	private String orgnr;
	private String applikasjon;
	private String opprettetAv;
	private ZonedDateTime opprettetTidspunkt;
}
