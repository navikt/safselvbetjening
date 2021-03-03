package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangDokumentInfoDto {
	private String dokumentinfoId;
	private String dokumentstatus;
	private String brevkode;
	private SkjermingTypeCode skjerming;
	private TilgangVariantDto variant;
}
