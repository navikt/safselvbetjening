package no.nav.safselvbetjening.tilgang.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.SkjermingTypeCode;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class UtledTilgangJournalpost {
	private final JournalStatusCode journalstatusCode;
	private final LocalDateTime datoOpprettet;
	private final LocalDateTime journalfoertDato;
	private final boolean feilregistrert;
	private final FagomradeCode fagomradeCode;
	private final SkjermingTypeCode skjerming;
	private final JournalpostTypeCode journalpostType;
	private final String avsenderMottakerId;
	private final String mottaksKanalCode;
	private final List<UtledTilgangDokument> utledTilgangDokumentList;
	private final UtledTilgangBruker utledTilgangBruker;
	private final UtledTilgangSak utledTilgangSak;
}
