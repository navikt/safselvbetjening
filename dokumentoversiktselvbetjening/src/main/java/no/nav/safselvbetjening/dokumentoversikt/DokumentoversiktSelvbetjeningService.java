package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterRequestTo;
import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.pdl.IdentConsumer;
import no.nav.safselvbetjening.consumer.sak.Arkivsak;
import no.nav.safselvbetjening.consumer.sak.ArkivsakConsumer;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Sakstema;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class DokumentoversiktSelvbetjeningService {
    private final IdentConsumer identConsumer;
    private final ArkivsakConsumer arkivsakConsumer;
    private final FagarkivConsumer fagarkivConsumer;
    private final JournalpostMapper journalpostMapper;

    public DokumentoversiktSelvbetjeningService(final IdentConsumer identConsumer,
                                                final ArkivsakConsumer arkivsakConsumer,
                                                final FagarkivConsumer fagarkivConsumer,
                                                final JournalpostMapper journalpostMapper) {
        this.identConsumer = identConsumer;
        this.arkivsakConsumer = arkivsakConsumer;
        this.fagarkivConsumer = fagarkivConsumer;
        this.journalpostMapper = journalpostMapper;
    }

    public Dokumentoversikt queryDokumentoversikt(final String ident, final List<String> tema) {
        if (ident == null) {
            return Dokumentoversikt.empty();
        }
        final List<String> aktoerIds = identConsumer.hentAktoerIder(ident);
        if (aktoerIds.isEmpty()) {
            return Dokumentoversikt.notFound();
        }
        final List<Arkivsak> arkivsaker = arkivsakConsumer.hentSakerByAktoerIds(aktoerIds);
        if (arkivsaker.isEmpty()) {
            return Dokumentoversikt.empty();
        }
        FinnJournalposterResponseTo finnJournalposterResponseTo = fagarkivConsumer.finnJournalposter(FinnJournalposterRequestTo.builder()
                .gsakSakIds(arkivsaker.stream().map(s -> s.getId().toString()).collect(Collectors.toList()))
                .fraDato("2016-06-04")
                .inkluderJournalpostType(Arrays.asList(JournalpostTypeCode.values()))
                .inkluderJournalStatus(Arrays.asList(JournalStatusCode.J, JournalStatusCode.E, JournalStatusCode.FL, JournalStatusCode.FS))
                .foerste(9999)
                .visFeilregistrerte(false)
                .build());

        Map<FagomradeCode, List<JournalpostDto>> temaMap = finnJournalposterResponseTo.getTilgangJournalposter().stream().collect(groupingBy(JournalpostDto::getFagomrade));
        List<Sakstema> sakstema = temaMap.entrySet().stream().map(fagomradeCodeListEntry ->
                Sakstema.builder()
                        .kode(fagomradeCodeListEntry.getKey().name())
                        .navn("TODO")
                        .journalposter(fagomradeCodeListEntry.getValue().stream().map(journalpostMapper::map).collect(Collectors.toList()))
                        .build()
        ).collect(Collectors.toList());
        return Dokumentoversikt.builder()
                .tema(sakstema)
                .code("ok")
                .build();
    }
}
