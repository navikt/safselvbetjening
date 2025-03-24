package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;

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
	LocalDateTime sorteringsDato;
	@Builder.Default
	List<RelevantDato> relevanteDatoer = new ArrayList<>();
	@Builder.Default
	List<DokumentInfo> dokumenter = new ArrayList<>();
	TilgangJournalpost tilgang;
}
