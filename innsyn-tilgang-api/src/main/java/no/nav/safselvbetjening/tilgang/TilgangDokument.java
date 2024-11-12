package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

/**
 * TilgangDokument representerer et dokument tilknyttet en journalpost, og inneholder sammen med TilgangJournalpost og
 * Tilgangvariant den nødvendige informasjonen for å avgjøre om en innlogget bruker har tilgang til å se dokument i
 * selvbetjeningsløsningen på nav.no. Feltene i TilgangDokument korresponderer med ArkivDokumentInfo-modellen fra dokarkiv
 * sitt api. Se de enkelte typene for statiske metoder som mapper fra dokarkiv-data til tilgangsdomenet.
 */
@Builder
public record TilgangDokument(
		long id,
		String kategori,
		boolean kassert,
		boolean hoveddokument,
		@NonNull TilgangSkjermingType skjerming,
		List<TilgangVariant> dokumentvarianter
) {
}
