package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import java.util.List;

public record ArkivDokumentinfo(Long dokumentInfoId, String skjerming, String kategori, Boolean kassert, List<ArkivFildetaljer> fildetaljer) {
}
