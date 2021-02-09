package no.nav.safselvbetjening;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

    private final Endpoints endpoints = new Endpoints();
    private final Serviceuser serviceuser = new Serviceuser();

    @Data
    @Validated
    public static class Endpoints {
        @NotEmpty
        private String pdl;
        @NotEmpty
        private String sak;
        @NotEmpty
        private String fagarkiv;
    }

    @Data
    @Validated
    public static class Serviceuser {
        @NotEmpty
        @ToString.Exclude
        private String username;
        @NotEmpty
        @ToString.Exclude
        private String password;
    }
}
