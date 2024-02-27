package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import java.util.List;

public record ArkivDokumentinfo(Long dokumentInfoId,
								String tilknyttetSom,
								String skjerming,
								String kategori,
								Boolean kassert,
								List<ArkivFildetaljer> fildetaljer,
								// kun metadata, ikke brukt til tilgangskontroll
								String tittel,
								String brevkode,
								Boolean sensitivt
) {
}
