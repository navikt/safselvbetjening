package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterRequestTo;
import no.nav.safselvbetjening.consumer.fagarkiv.FinnJournalposterResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.FagomradeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.pdl.IdentConsumer;
import no.nav.safselvbetjening.consumer.sak.ArkivsakConsumer;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.service.SakService;
import no.nav.safselvbetjening.service.Saker;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class DokumentoversiktSelvbetjeningService {
    private final SafSelvbetjeningProperties safSelvbetjeningProperties;
    private final IdentConsumer identConsumer;
    private final ArkivsakConsumer arkivsakConsumer;
    private final IdentService identService;
    private final SakService sakService;
    private final FagarkivConsumer fagarkivConsumer;
    private final JournalpostMapper journalpostMapper;

    public DokumentoversiktSelvbetjeningService(final SafSelvbetjeningProperties safSelvbetjeningProperties,
                                                final IdentConsumer identConsumer,
                                                final ArkivsakConsumer arkivsakConsumer,
                                                final IdentService identService,
                                                final SakService sakService,
                                                final FagarkivConsumer fagarkivConsumer,
                                                final JournalpostMapper journalpostMapper) {
        this.safSelvbetjeningProperties = safSelvbetjeningProperties;
        this.identConsumer = identConsumer;
        this.arkivsakConsumer = arkivsakConsumer;
        this.identService = identService;
        this.sakService = sakService;
        this.fagarkivConsumer = fagarkivConsumer;
        this.journalpostMapper = journalpostMapper;
    }

    public Dokumentoversikt queryDokumentoversikt(final String ident, final List<String> tema) {
        log.info("dokumentoversiktSelvbetjening henter dokumentoversikt til person.");
        if (isBlank(ident)) {
            return Dokumentoversikt.notFound();
        }

        final BrukerIdenter brukerIdenter = identService.hentIdenter(ident);
        if (brukerIdenter.isEmpty()) {
            return Dokumentoversikt.notFound();
        }
        final Saker saker = sakService.hentSaker(brukerIdenter, tema);
        if (saker.isNone()) {
            return Dokumentoversikt.empty();
        }

        FinnJournalposterResponseTo finnJournalposterResponseTo = fagarkivConsumer.finnJournalposter(FinnJournalposterRequestTo.builder()
                .alleIdenter(brukerIdenter.getFoedselsnummer())
                .psakSakIds(saker.getPensjonSakIds())
                .gsakSakIds(saker.getArkivSakIds())
                .fraDato(safSelvbetjeningProperties.getTidligstInnsynDato())
                .inkluderJournalpostType(Arrays.asList(JournalpostTypeCode.values()))
                .inkluderJournalStatus(Arrays.asList(JournalStatusCode.MO, JournalStatusCode.MO, JournalStatusCode.J, JournalStatusCode.E, JournalStatusCode.FL, JournalStatusCode.FS))
                .foerste(9999)
                .visFeilregistrerte(false)
                .build());

        // tilgangskontroll

        Map<FagomradeCode, List<JournalpostDto>> temaMap = finnJournalposterResponseTo.getTilgangJournalposter().stream()
                .collect(groupingBy(JournalpostDto::getFagomrade));
        List<Sakstema> sakstema = temaMap.entrySet().stream()
                .map(this::mapSakstema)
                .sorted(Comparator.comparing(Sakstema::getKode))
                .collect(Collectors.toList());
        log.info("dokumentoversiktSelvbetjening hentet dokumentoversikt til person. sakstema={}, journalposter={}", sakstema.size(),
                finnJournalposterResponseTo.getTilgangJournalposter().size());
        return Dokumentoversikt.builder()
                .tema(sakstema)
                .code("ok")
                .build();
    }

    private Sakstema mapSakstema(Map.Entry<FagomradeCode, List<JournalpostDto>> fagomradeCodeListEntry) {
        final Tema tema = FagomradeCode.toTema(fagomradeCodeListEntry.getKey());
        return Sakstema.builder()
                        .kode(tema.name())
                        .navn(tema.getTemanavn())
                        .journalposter(fagomradeCodeListEntry.getValue().stream()
                                .filter(Objects::nonNull)
                                .map(journalpostMapper::map).collect(Collectors.toList()))
                        .build();
    }
}
