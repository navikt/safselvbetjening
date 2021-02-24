package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FL;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.J;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.M;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.MO;

@Component
public class UtledTilgangService {

	public UtledTilgangService() {
	}

	public FinnJournalposterResponseTo utledTilgangJournalpost(FinnJournalposterResponseTo finnJournalposterResponseTo,
															   List<String> aktoerIds) {

		finnJournalposterResponseTo.getTilgangJournalposter().forEach(
				journalpostDto -> {
					if (!isBrukerPart(journalpostDto, aktoerIds)) {
						finnJournalposterResponseTo.getTilgangJournalposter().remove(journalpostDto);
					}
				}
		);
		return finnJournalposterResponseTo;
	}

	private boolean isBrukerPart(JournalpostDto journalpostDto, List<String> aktoerIds) {

		JournalStatusCode journalStatusCode = journalpostDto.getJournalstatus();

		if (journalStatusCode == M || journalStatusCode == MO) {
			return aktoerIds.contains(journalpostDto.getBruker().getBrukerId());
		} else if (journalStatusCode == FS || journalStatusCode == FL || journalStatusCode == J || journalStatusCode == E) {
			if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.FS22) {
				return aktoerIds.contains(journalpostDto.getSaksrelasjon().getAktoerId());
			} else if (journalpostDto.getSaksrelasjon().getFagsystem() == FagsystemCode.PEN) {
				//todo: Sjekk brukers identer mot PSAK-ident (hva er PSAK-ident?)
			}
		}
		return false;
	}
}
