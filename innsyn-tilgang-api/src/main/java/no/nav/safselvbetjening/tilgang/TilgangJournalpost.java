package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.VISES;
import static no.nav.safselvbetjening.tilgang.UtledTilgangService.isBlank;

/**
 * TilgangJournalpost representerer en journalpost, og inneholder den nødvendige informasjonen for å avgjøre om en
 * innlogget bruker har tilgang til å se en journalpost eller et dokument i selvbetjeningsløsningen på nav.no.
 * Feltene i TilgangJournalpost korresponderer med ArkivJournalpost-modellen fra dokarkiv sitt api.
 * Se de enkelte typene for statiske metoder som mapper fra dokarkiv-data til tilgangsdomenet.
 */
@Value
@Builder
public class TilgangJournalpost {
	long journalpostId;
	TilgangJournalstatus journalstatus;
	TilgangJournalposttype journalposttype;
	@NonNull
	LocalDateTime datoOpprettet;
	LocalDateTime journalfoertDato;
	String tema;
	@NonNull
	TilgangMottakskanal mottakskanal;
	@ToString.Exclude
	Ident avsenderMottakerId;
	TilgangSkjermingType skjerming;
	TilgangSak tilgangSak;
	TilgangBruker tilgangBruker;
	@NonNull
	TilgangInnsyn innsyn;
	@NonNull
	@Builder.Default
	List<TilgangDokument> dokumenter = new ArrayList<>();

	/**
	 * Tema på sakstilknytning er prioritert over tema på journalpost
	 */
	public String getGjeldendeTema() {
		if (tilgangSak == null) {
			return tema;
		} else {
			if (isBlank(tilgangSak.getTema())) {
				return tema;
			} else {
				return tilgangSak.getTema();
			}
		}
	}

	public boolean innsynSkjules() {
		return SKJULES.contains(innsyn);
	}

	public boolean innsynVises() {
		return VISES.contains(innsyn);
	}
}
