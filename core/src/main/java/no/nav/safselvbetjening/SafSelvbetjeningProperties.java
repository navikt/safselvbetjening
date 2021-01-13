package no.nav.safselvbetjening;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Data
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

    private final Endpoints endpoints = new Endpoints();

    @Data
    @Validated
    public static class Endpoints {
        @NotEmpty
        private String pdl;
    }
}
