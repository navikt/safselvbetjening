package no.nav.safselvbetjening.azure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@Validated
@ConfigurationProperties(prefix = "azure.app")
public record AzureProperties(
		@NotEmpty String tokenUrl,
		@NotEmpty String clientId,
		@NotEmpty String clientSecret,
		@NotEmpty String tenantId,
		@NotEmpty String wellKnownUrl
) {
	public static final String SPRING_DEFAULT_PRINCIPAL = "anonymousUser";
	public static final String CLIENT_REGISTRATION_ID_PDL = "azure-pdl";
	public static final String CLIENT_REGISTRATION_ID_PENSJON = "azure-pensjon";

	public static OAuth2AuthorizeRequest getOAuth2AuthorizeRequestForAzure(String clientRegistrationId) {
		return OAuth2AuthorizeRequest
				.withClientRegistrationId(clientRegistrationId)
				.principal(SPRING_DEFAULT_PRINCIPAL)
				.build();
	}

}
