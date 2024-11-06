package no.nav.safselvbetjening.tilgang;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

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
