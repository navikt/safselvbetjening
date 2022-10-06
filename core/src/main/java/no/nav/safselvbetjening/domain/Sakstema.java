package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class Sakstema {
    @Builder.Default
    List<Journalpost> journalposter = new ArrayList<>();
    String kode;
    String navn;
}
