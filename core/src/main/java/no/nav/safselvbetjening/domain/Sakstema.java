package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Sakstema {
    @Builder.Default
    private final List<Journalpost> journalposter = new ArrayList<>();
    private final String kode;
    private final String navn;
}
