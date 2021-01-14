package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Dokumentoversikt {
    @Builder.Default
    private final List<Sakstema> sakstema = new ArrayList<>();
    private final String code;

    public static Dokumentoversikt empty() {
        return new Dokumentoversikt(new ArrayList<>(), "ok");
    }
}
