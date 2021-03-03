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

    /**
     * Cut-off dato for innsyn fra innbygger. Dokumenter før denne dato skal ikke hentes eller vises.
     */
    @NotEmpty
    private String tidligstInnsynDato;

    @Data
    @Validated
    public static class Endpoints {
        /**
         * URL til PDL (Persondataløsningen).
         */
        @NotEmpty
        private String pdl;
        /**
         * URL til sak API.
         */
        @NotEmpty
        private String sak;
        /**
         * URL til oppslagstjenesten i fagarkivet.
         */
        @NotEmpty
        private String fagarkiv;
        /**
         * URL til PensjonSak_v1 SOAP tjenesten.
         */
        @NotEmpty
        private String pensjon;
        /**
         * URL til SAML STS tjenesten. Veksler UsernameToken til SAML token.
         */
        @NotEmpty
        private String samlsts;
    }

    @Data
    @Validated
    public static class Serviceuser {
        /**
         * Brukernavn til onprem AD servicebruker.
         */
        @NotEmpty
        @ToString.Exclude
        private String username;
        /**
         * Passord til onprem AD servicebruker.
         */
        @NotEmpty
        @ToString.Exclude
        private String password;
    }
}
