package no.nav.safselvbetjening.azure;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.validation.annotation.Validated;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@Validated
@ConfigurationProperties(prefix = "azure")
public record AzureProperties(
		@NotEmpty String appClientId,
		@NotEmpty String appClientSecret,
		@NotEmpty String openidConfigTokenEndpoint
) {
	public static final String SPRING_DEFAULT_PRINCIPAL = "anonymousUser";
	public static final String CLIENT_REGISTRATION_DOKARKIV = "azure-dokarkiv";
	public static final String CLIENT_REGISTRATION_PDL = "azure-pdl";
	public static final String CLIENT_REGISTRATION_PENSJON = "azure-pensjon";

	public static OAuth2AuthorizeRequest getOAuth2AuthorizeRequestForAzure(String clientRegistrationId) {
		return OAuth2AuthorizeRequest
				.withClientRegistrationId(clientRegistrationId)
				.principal(SPRING_DEFAULT_PRINCIPAL)
				.build();
	}
}
