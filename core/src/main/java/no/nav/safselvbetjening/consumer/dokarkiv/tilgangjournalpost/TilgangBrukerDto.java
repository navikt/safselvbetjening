package no.nav.safselvbetjening.consumer.dokarkiv.tilgangjournalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangBrukerDto {
	private String brukerId;
	private String brukerType;
}
