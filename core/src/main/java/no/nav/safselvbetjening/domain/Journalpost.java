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
	private final String journalpostId;
	@ToString.Exclude
	private final String tittel;
	private final Journalposttype journalposttype;
	private final Journalstatus journalstatus;
	private final String tema;
	private final Sak sak;
	private final AvsenderMottaker avsender;
	private final AvsenderMottaker mottaker;
	private final Kanal kanal;
	private final String eksternReferanseId;
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
