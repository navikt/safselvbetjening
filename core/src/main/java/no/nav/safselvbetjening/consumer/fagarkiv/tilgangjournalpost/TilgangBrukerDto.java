package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
public class TilgangBrukerDto {
	private String brukerId;
	private String brukerType;
}
