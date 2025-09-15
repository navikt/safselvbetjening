package no.nav.safselvbetjening;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

    @Valid
    private final Endpoints endpoints = new Endpoints();
    @Valid
    private final Topics topics = new Topics();

    @Data
    public static class Endpoints {
        @NotNull
        @Valid
        private AzureEndpoint pdl;

        @NotNull
        @Valid
        private AzureEndpoint sak;

        @NotNull
        @Valid
        private AzureEndpoint dokarkiv;

        @NotNull
        @Valid
        private AzureEndpoint pensjon;

        @NotNull
        @Valid
        private TokenXEndpoint reprApi;

        @NotEmpty
        private String fagarkiv;
    }

    @Data
    public static class AzureEndpoint {
        @NotEmpty
        private String url;

        @NotEmpty
        private String scope;
    }

    @Data
    public static class TokenXEndpoint {
        @NotEmpty
        private String url;

        @NotEmpty
        private String scope;
    }

    @Data
    public static class Topics {
        @NotEmpty
        private String dokdistdittnav;

    }

}