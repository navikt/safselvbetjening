package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Value
@Builder
public class Journalpost {
	private final String journalpostId;
	private final String tittel;
	private final Journalposttype journalposttype;
	private final Journalstatus journalstatus;
	private final AvsenderMottaker avsenderMottaker;
	private final Kanal kanal;
	@Builder.Default
	private final List<RelevantDato> relevanteDatoer = new ArrayList<>();
	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();
	private final TilgangJournalpost tilgang;

	@Data
	@Builder
	public static class TilgangJournalpost {
		private final LocalDateTime datoOpprettet;
		private final LocalDateTime journalfoertDato;
		private final String fagomradeCode;
		private final SkjermingType skjerming;
		private final TilgangSak tilgangSak;
		private final TilgangBruker tilgangBruker;
	}

	@Data
	@Builder
	public static class TilgangSak {
		private final String tema;
		private final String fagsystem;
		private final String aktoerId;
		private final String foedselsnummer;
		private final boolean feilregistrert;
	}

	@Data
	@Builder
	public static class TilgangBruker{
		private final String brukerId;
	}
}
