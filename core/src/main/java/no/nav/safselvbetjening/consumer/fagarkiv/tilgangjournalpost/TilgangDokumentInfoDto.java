package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangDokumentInfoDto {
	private String dokumentinfoId;
	private String dokumentstatus;
	private String brevkode;
	private String kategori;
	private Boolean organinternt;
	private Boolean innskrenketPartsinnsyn;
	private Boolean innskrenketTredjepart;
	private Boolean kassert;
	private SkjermingTypeCode skjerming;
	private TilgangVariantDto variant;
}
