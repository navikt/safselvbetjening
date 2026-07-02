package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.representasjon.Representasjonsforhold;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;

import java.util.List;
import java.util.Optional;

import static no.nav.safselvbetjening.domain.Journalposttype.U;
import static no.nav.safselvbetjening.domain.Kanal.NAV_NO;

record Tilgangskontroll(Journalposttype journalpostType, Kanal kanal, boolean isHoveddokument,
						TilgangVariantFormat determinedVariantFormat,
						Optional<Representasjonsforhold> representasjonsforhold) {

	public Tilgangskontroll(Journalpost journalpost, TilgangVariantFormat determinedVariantFormat, Optional<Representasjonsforhold> representasjonsforholdOpt) {
		this(journalpost.getJournalposttype(),
				journalpost.getKanal(),
				isHoveddokument(journalpost.getDokumenter()),
				determinedVariantFormat,
				representasjonsforholdOpt);
	}

	private static boolean isHoveddokument(List<DokumentInfo> dokumenter) {
		if (dokumenter.isEmpty()) {
			return false;
		}
		return dokumenter.getFirst().isHoveddokument();
	}

	boolean genererHoveddokumentLestHendelse() {
		return journalpostType == U &&
				kanal == NAV_NO &&
				isHoveddokument &&
				representasjonsforhold.isEmpty();
	}
}
