package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.fullmektig.Fullmakt;

import java.util.Optional;
import java.util.Set;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.domain.Journalposttype.U;

record Tilgangskontroll(Journalposttype journalpostType, String tilgangJournalstatus,
						Optional<Fullmakt> fullmakt) {
	private static final Set<String> JOURNALSTATUS_GENERER_HOVEDDOKUMENTLEST_HENDELSE = Set.of(FS.name(), E.name());

	boolean genererHoveddokumentLestHendelse() {
		return journalpostType == U &&
			   JOURNALSTATUS_GENERER_HOVEDDOKUMENTLEST_HENDELSE.contains(tilgangJournalstatus) &&
			   fullmakt.isEmpty();
	}
}
