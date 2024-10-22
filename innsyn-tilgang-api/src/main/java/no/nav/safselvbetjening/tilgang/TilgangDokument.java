package no.nav.safselvbetjening.tilgang;

import lombok.Builder;

import java.util.List;

@Builder
public record TilgangDokument(
		long id,
		String kategori,
		boolean kassert,
		TilgangSkjermingType skjerming,
		List<TilgangVariant> dokumentvarianter
) {
}
