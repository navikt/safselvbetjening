package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.NonNull;

/**
 * TilgangVariant representerer en fil for et dokument i en journalpost, og inneholder sammen med TilgangJournalpost og
 * TilgangDokument den nødvendige informasjonen for å avgjøre om en innlogget bruker har tilgang til å se den filen i
 * selvbetjeningsløsningen på nav.no. Feltene i TilgangVariant korresponderer med ArkivFildetaljer-modellen fra dokarkiv sitt api.
 * Se de enkelte typene for statiske metoder som mapper fra dokarkiv-data til tilgangsdomenet.
 */
@Builder
public record TilgangVariant(
		@NonNull TilgangSkjermingType skjerming,
		@NonNull TilgangVariantFormat variantformat
) {
}
