package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.VISES;
import static no.nav.safselvbetjening.tilgang.UtledTilgangService.isBlank;

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
	String mottakskanal;
	@ToString.Exclude
	String avsenderMottakerId;
	TilgangSkjermingType skjerming;
	TilgangSak tilgangSak;
	TilgangBruker tilgangBruker;
	TilgangInnsyn innsyn;
	@NonNull @Builder.Default
	List<TilgangDokument> dokumenter = new ArrayList<>();

	/**
	 * Tema på sakstilknytning er prioritert over tema på journalpost
	 */
	public String getGjeldendeTema() {
		if (tilgangSak == null) {
			return tema;
		} else {
			if (isBlank(tilgangSak.tema())) {
				return tema;
			} else {
				return tilgangSak.tema();
			}
		}
	}

	public boolean innsynSkjules() {
		if (innsyn == null) {
			return false;
		} else {
			return SKJULES.contains(innsyn);
		}
	}

	public boolean innsynVises() {
		if (innsyn == null) {
			return false;
		} else {
			return VISES.contains(innsyn);
		}
	}
}
