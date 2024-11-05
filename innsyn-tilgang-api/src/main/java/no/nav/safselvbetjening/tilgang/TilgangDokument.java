package no.nav.safselvbetjening.tilgang;

import lombok.Builder;

import java.util.List;

@Builder
public record TilgangDokument(
		long id,
		String kategori,
		boolean kassert,
		boolean hoveddokument,
		TilgangSkjermingType skjerming,
		List<TilgangVariant> dokumentvarianter
) {
}
