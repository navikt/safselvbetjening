package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Value
@Builder
public class Journalpost {
	String journalpostId;
	@ToString.Exclude
	String tittel;
	Journalposttype journalposttype;
	Journalstatus journalstatus;
	String tema;
	Sak sak;
	AvsenderMottaker avsender;
	AvsenderMottaker mottaker;
	Kanal kanal;
	String eksternReferanseId;
	@Builder.Default
	List<RelevantDato> relevanteDatoer = new ArrayList<>();
	@Builder.Default
	List<DokumentInfo> dokumenter = new ArrayList<>();
	TilgangJournalpost tilgang;

	@Data
	@Builder
	public static class TilgangJournalpost {
		private final String journalstatus;
		private final LocalDateTime datoOpprettet;
		private final LocalDateTime journalfoertDato;
		private final String tema;
		private final Kanal mottakskanal;
		@ToString.Exclude
		private final String avsenderMottakerId;
		private final SkjermingType skjerming;
		private final TilgangSak tilgangSak;
		private final TilgangBruker tilgangBruker;
		private final Innsyn innsyn;
	}

	@Data
	@Builder
	public static class TilgangSak {
		private final String tema;
		private final String fagsystem;
		// Populert for arkivsaksystem gsak
		private final String aktoerId;
		// Populert for arkivsaksystem pensjon
		private final String foedselsnummer;
		private final boolean feilregistrert;
	}

	@Data
	@Builder
	public static class TilgangBruker {
		@ToString.Exclude
		private final String brukerId;
	}
}
