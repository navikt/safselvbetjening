package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class Dokumentoversikt {
    @Builder.Default
    List<Sakstema> tema = new ArrayList<>();

    @Builder.Default
    List<Fagsak> fagsak = new ArrayList<>();

    @Builder.Default
    List<Journalpost> journalposter = new ArrayList<>();

    public static Dokumentoversikt empty() {
        return  Dokumentoversikt.builder().build();
    }
}
