package no.nav.safselvbetjening.tokendings;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.safselvbetjening.cache.CacheConfig.TOKENDINGS_CACHE;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Component
public class TokendingsConsumer {

	private final RestClient restClient;
	private final TokendingsProperties tokendingsProperties;

	public TokendingsConsumer(RestClient.Builder restClientBuilder,
							  TokendingsProperties tokendingsProperties) {
		this.restClient = restClientBuilder
				.baseUrl(tokendingsProperties.getTokenEndpoint())
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.tokendingsProperties = tokendingsProperties;
	}

	@Cacheable(value = TOKENDINGS_CACHE, key = "T(no.nav.safselvbetjening.tokendings.TokendingsConsumer).hashedCacheKey(#subjectToken, #scope)")
	public TokenResponse exchange(final String subjectToken, final String scope) {
		MultiValueMap<String, String> formMultiValueData = new LinkedMultiValueMap<>();
		formMultiValueData.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
		formMultiValueData.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
		formMultiValueData.add("client_assertion", clientAssertion());
		formMultiValueData.add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt");
		formMultiValueData.add("subject_token", subjectToken);
		formMultiValueData.add("audience", scope);

		return restClient.post()
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formMultiValueData)
				.retrieve()
				.body(TokenResponse.class);
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), UTF_8);
		if (response.getStatusCode().is4xxClientError()) {
			throw new TokenException(
					format("Klarte ikke hente token fra Tokendings. Feilet med statuskode=%s Feilmelding=%s",
							response.getStatusCode().value(), response));
		}
		throw new TokenTechnicalException(
				format("Kall mot Tokendings feilet med feilmelding=%s", body));
	}

	String clientAssertion() {
		try {
			Date now = Date.from(Instant.now());
			JWSHeader jwsHeader = new JWSHeader.Builder(RS256)
					.keyID(tokendingsProperties.getRsaKey().getKeyID())
					.type(JWT).build();
			JWTClaimsSet jwsClaims = new JWTClaimsSet.Builder()
					.issuer(tokendingsProperties.getClientId())
					.subject(tokendingsProperties.getClientId())
					.audience(tokendingsProperties.getTokenEndpoint())
					.issueTime(now)
					.expirationTime(Date.from(Instant.now().plusSeconds(60)))
					.jwtID(UUID.randomUUID().toString())
					.notBeforeTime(now)
					.build();
			SignedJWT signedJWT = new SignedJWT(jwsHeader, jwsClaims);
			signedJWT.sign(new RSASSASigner(tokendingsProperties.getRsaKey()));
			return signedJWT.serialize();
		} catch (JOSEException e) {
			throw new ConsumerFunctionalException("Klarte ikke signere JWT", e);
		}
	}

	@SuppressWarnings("unused")
	public static String hashedCacheKey(String token, String scope) {
		return sha256Hex(token + scope);
	}
}
