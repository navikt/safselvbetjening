package no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.InnsynCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;

import java.time.LocalDateTime;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangJournalpostDto {
	private String journalpostId;
	private JournalStatusCode journalStatus;
	private JournalpostTypeCode journalpostType;
	private FagomradeCode fagomrade;
	private LocalDateTime datoOpprettet;
	private LocalDateTime journalfoertDato;
	private MottaksKanalCode mottakskanal;
	private String avsenderMottakerId;
	private TilgangBrukerDto bruker;
	private TilgangSakDto sak;
	private SkjermingTypeCode skjerming;
	private TilgangDokumentInfoDto dokument;
	private InnsynCode innsyn;

	public boolean isTilknyttetPensjonsak() {
		if(sak == null) {
			return false;
		}
		return PEN.name().equals(sak.getFagsystem());
	}
}
