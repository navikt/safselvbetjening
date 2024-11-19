package no.nav.safselvbetjening;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Data
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

    private final Endpoints endpoints = new Endpoints();
    private final Serviceuser serviceuser = new Serviceuser();
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
        @NotNull
        private AzureEndpoint pdl;

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
         * URL til safintern oppslagstjenesten i dokarkiv.
         */
        @NotNull
        private AzureEndpoint dokarkiv;

        /**
         * URL til PEN (pensjon).
         */
        @NotNull
        private AzureEndpoint pensjon;

        @NotNull
        private TokenXEndpoint reprApi;
    }

    @Data
    @Validated
    public static class AzureEndpoint {
        /**
         * Url til tjeneste som har azure autorisasjon
         */
        @NotEmpty
        private String url;
        /**
         * Scope til azure client credential flow
         */
        @NotEmpty
        private String scope;
    }

    @Data
    @Validated
    public static class TokenXEndpoint {
        /**
         * Url til tjeneste som har tokenx autorisasjon
         */
        @NotEmpty
        private String url;
        /**
         * Scope til tokenx exchange flow
         */
        @NotEmpty
        private String scope;
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
    public static class Topics {
    	/**
    	 * Kafka topic for kommunikasjon mot dokdistdittnav
    	 */
        @NotEmpty
        private String dokdistdittnav;

    }
}
