package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.fullmektig.Fullmakt;

import java.util.List;
import java.util.Optional;

import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;

record Tilgangskontroll(Journalposttype journalpostType, Kanal kanal, boolean isHoveddokument,
						Optional<Fullmakt> fullmakt) {

	public Tilgangskontroll(Journalpost journalpost, Optional<Fullmakt> fullmaktOpt) {
		this(journalpost.getJournalposttype(),
				journalpost.getKanal(),
				isHoveddokument(journalpost.getDokumenter()),
				fullmaktOpt);
	}

	private static boolean isHoveddokument(List<DokumentInfo> dokumenter) {
		if(dokumenter.isEmpty()) {
			return false;
		}
		return dokumenter.get(0).isHoveddokument();
	}

	boolean genererHoveddokumentLestHendelse() {
		return journalpostType == U &&
			   kanal == NAV_NO &&
			   isHoveddokument &&
			   fullmakt.isEmpty();
	}
}
