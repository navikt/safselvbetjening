package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.pdl.PdlIdentConsumer;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentoversikt;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Variantformat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class DokumentoversiktSelvbetjeningService {
    public Dokumentoversikt queryDokumentoversikt(final List<String> tema) {
        return stub();
    }

    // FIXME
    Dokumentoversikt stub() {
        return Dokumentoversikt.builder().code("ok")
                .tema(Collections.singletonList(Sakstema.builder()
                        .kode("FOR")
                        .navn("Foreldrepenger")
                        .journalposter(Collections.singletonList(
                                Journalpost.builder()
                                        .journalpostId("10000000")
                                        .journalposttype(Journalposttype.I)
                                        .tittel("Søknad om foreldrepenger")
                                        .journalstatus(Journalstatus.JOURNALFOERT)
                                        .kanal(Kanal.NAV_NO)
                                        .avsenderMottaker(AvsenderMottaker.builder()
                                                .id("11111111111")
                                                .type(AvsenderMottakerIdType.FNR)
                                                .build())
                                        .relevanteDatoer(Arrays.asList(
                                                new RelevantDato(LocalDateTime.now().minusDays(2), Datotype.DATO_REGISTRERT),
                                                new RelevantDato(LocalDateTime.now().minusDays(2), Datotype.DATO_DOKUMENT),
                                                new RelevantDato(LocalDateTime.now(), Datotype.DATO_JOURNALFOERT)))
                                        .dokumenter(Arrays.asList(
                                                DokumentInfo.builder()
                                                        .dokumentInfoId("2000000")
                                                        .tittel("Søknad om foreldrepenger")
                                                        .brevkode("SØK-123")
                                                        .dokumentvarianter(Collections.singletonList(Dokumentvariant.builder()
                                                                .brukerHarTilgang(true)
                                                                .code("ok")
                                                                .filuuid(UUID.randomUUID().toString())
                                                                .variantformat(Variantformat.ARKIV)
                                                                .build()))
                                                        .build(),
                                                DokumentInfo.builder()
                                                        .dokumentInfoId("2000001")
                                                        .tittel("Lønnsslipp for Bjarne Betjent")
                                                        .brevkode("LØNN-123")
                                                        .dokumentvarianter(Collections.singletonList(Dokumentvariant.builder()
                                                                .brukerHarTilgang(true)
                                                                .code("ok")
                                                                .filuuid(UUID.randomUUID().toString())
                                                                .variantformat(Variantformat.ARKIV)
                                                                .build()))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build()))
                .build();
    }
}
