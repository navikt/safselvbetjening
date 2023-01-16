package no.nav.safselvbetjening.azure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@Validated
@ConfigurationProperties(prefix = "azure")
public record AzureProperties(
		@NotEmpty String appClientId,
		@NotEmpty String appClientSecret,
		@NotEmpty String appTenantId,
		@NotEmpty String appWellKnownUrl,
		@NotEmpty String openidConfigTokenEndpoint
) {
}
