package no.nav.safselvbetjening.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.domain.kode.Journalposttype;
import no.nav.safselvbetjening.domain.kode.Journalstatus;
import no.nav.safselvbetjening.domain.kode.Kanal;

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
	private final List<RelevantDato> relevanteDatoer = new ArrayList<>();
	private final List<DokumentInfo> dokumenter = new ArrayList<>();
}
