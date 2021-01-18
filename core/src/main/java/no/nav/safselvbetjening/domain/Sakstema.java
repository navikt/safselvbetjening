package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class Sakstema {
    @Builder.Default
    private final List<Journalpost> journalposter = new ArrayList<>();
    private final String kode;
    private final String navn;
}
