package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class Dokumentoversikt {
    @Builder.Default
    private final List<Sakstema> tema = new ArrayList<>();
    private final String code;

    public static Dokumentoversikt empty() {
        return new Dokumentoversikt(new ArrayList<>(), "ok");
    }
}
