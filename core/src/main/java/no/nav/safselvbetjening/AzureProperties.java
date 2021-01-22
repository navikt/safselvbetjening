package no.nav.safselvbetjening;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@ConfigurationProperties("azure.app")
@Validated
/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
public class AzureProperties {
    @NotEmpty
    private String tokenUrl;
    @NotEmpty
    private String clientId;
    @NotEmpty
    private String clientSecret;
    @NotEmpty
    private String tenantId;
    @NotEmpty
    private String wellKnownUrl;
    @NotEmpty
    private String scope;
}
