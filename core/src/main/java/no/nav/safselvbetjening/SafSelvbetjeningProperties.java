package no.nav.safselvbetjening;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Data
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

    private final Endpoints endpoints = new Endpoints();
    private final Serviceuser serviceuser = new Serviceuser();
    private final Proxy proxy = new Proxy();
    private final Topics topics = new Topics();

    /**
     * Cut-off dato for innsyn fra innbygger. Dokumenter før denne dato skal ikke hentes eller vises.
     */
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tidligstInnsynDato;

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
        /**
         * URL til PensjonSakRestconsumer.
         */
        @NotEmpty
        private String pensjonsak;
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

    @Data
    @Validated
    public static class Proxy {
        private String host;
        private int port;

        public boolean isSet() {
            return isNotBlank(host);
        }
    }

    @Data
    @Validated
    public static class Topics {
    	/**
    	 * Kafka topic for kommunikasjon mot dokdistdittnav
    	 */
        @NotEmpty
        private String dokdistdittnav;

    }
}
