package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.SKJULES;
import static no.nav.safselvbetjening.tilgang.TilgangInnsyn.VISES;
import static no.nav.safselvbetjening.tilgang.UtledTilgangService.isBlank;

@Data
@Builder
public class TilgangJournalpost {
	private final long journalpostId;
	private final TilgangJournalstatus journalstatus;
	private final TilgangJournalposttype journalposttype;
	private final LocalDateTime datoOpprettet;
	private final LocalDateTime journalfoertDato;
	private final String tema;
	private final String mottakskanal;
	@ToString.Exclude
	private final String avsenderMottakerId;
	private final TilgangSkjermingType skjerming;
	private final TilgangSak tilgangSak;
	private final TilgangBruker tilgangBruker;
	private final TilgangInnsyn innsyn;
	private final List<TilgangDokument> dokumenter;

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
