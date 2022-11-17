package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalpostDto {
	private Long journalpostId;
	private Long prevJournalpostId;
	private Long nextJournalpostId;
	private Long totaltAntall;
	private String innhold;
	private FagomradeCode fagomrade;
	private String behandlingstema;
	private String behandlingstemanavn;
	private JournalStatusCode journalstatus;
	private String avsenderMottakerId;
	private AvsenderMottakerIdTypeCode avsenderMottakerIdType;
	private String avsenderMottakerNavn;
	private String avsenderMottakerLand;
	private String journalforendeEnhet;
	private String journalfortAvNavn;
	private String opprettetAvNavn;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private JournalpostTypeCode journalposttype;
	private SaksrelasjonDto saksrelasjon;
	private BrukerDto bruker;
	private Date datoOpprettet;
	private Date mottattDato;
	private Date journalDato;
	private Date dokumentDato;
	private Date avsReturDato;
	private Date sendtPrintDato;
	private Date ekspedertDato;
	private SkjermingTypeCode skjerming;
	private List<TilleggsopplysningDto> tilleggsopplysninger;
	private List<DokumentInfoDto> dokumenter;
	private String antallRetur;
	private String kanalReferanseId;
	private InnsynCode innsyn;

	public boolean isTilknyttetSak() {
		return saksrelasjon != null && !isBlank(saksrelasjon.getSakId());
	}

	public boolean isMidlertidig() {
		return journalstatus == JournalStatusCode.M || journalstatus == JournalStatusCode.MO;
	}
}
