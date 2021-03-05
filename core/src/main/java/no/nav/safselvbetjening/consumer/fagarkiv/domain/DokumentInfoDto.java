package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DokumentInfoDto {
	private String dokumentInfoId;
	private Date datoFerdigstilt;
	private String brevkode;
	private String dokumenttypeId;
	private String tittel;
	private SkjermingTypeCode skjerming;
	private List<VariantDto> varianter;
	private Long origJournalpostId;
	private List<LogiskVedleggDto> logiske;
	private Boolean kassert;
	private DokumentKategoriCode kategori;
	private Boolean organInternt;
	private Boolean innskrPartsinnsyn;
}
