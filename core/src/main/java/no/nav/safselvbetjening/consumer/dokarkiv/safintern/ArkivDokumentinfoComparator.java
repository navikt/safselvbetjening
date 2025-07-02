package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import java.util.Comparator;
import java.util.Objects;

import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostMapper.TILKNYTTET_SOM_HOVEDDOKUMENT;

/**
 * Samme implementasjon som saf
 */
public final class ArkivDokumentinfoComparator implements Comparator<ArkivDokumentinfo> {
	@Override
	public int compare(ArkivDokumentinfo o1, ArkivDokumentinfo o2) {
		if (TILKNYTTET_SOM_HOVEDDOKUMENT.equalsIgnoreCase(o1.tilknyttetSom())) {
			return -1;
		} else if (TILKNYTTET_SOM_HOVEDDOKUMENT.equalsIgnoreCase(o2.tilknyttetSom())) {
			return 1;
		} else if (!Objects.equals(o1.rekkefoelge(), o2.rekkefoelge())) {
			if (o1.rekkefoelge() == null) {
				return 1;
			} else if (o2.rekkefoelge() == null) {
				return -1;
			} else {
				return o1.rekkefoelge().compareTo(o2.rekkefoelge());
			}
		} else {
			if (o1.dokumentInfoId() == null && o2.dokumentInfoId() == null) {
				return 0;
			} else if (o1.dokumentInfoId() == null) {
				return -1;
			} else if (o2.dokumentInfoId() == null) {
				return 1;
			}
			return o1.dokumentInfoId().compareTo(o2.dokumentInfoId());
		}
	}
}
