package no.nav.safselvbetjening.tokendings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static java.lang.String.format;
import static no.nav.safselvbetjening.cache.CacheConfig.TOKENDINGS_CACHE;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Component
public class TokendingsConsumer {

	private final WebClient webClient;
	private final TokendingsProperties tokendingsProperties;
	private final ObjectMapper objectMapper;

	public TokendingsConsumer(WebClient webClient, TokendingsProperties tokendingsProperties, ObjectMapper objectMapper) {
		this.webClient = webClient.mutate()
				.baseUrl(tokendingsProperties.getTokenEndpoint())
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
		this.tokendingsProperties = tokendingsProperties;
		this.objectMapper = objectMapper;
	}

	@Cacheable(value = TOKENDINGS_CACHE, key = "TokendingsConsumer.hashedCacheKey(#subjectToken, #scope)")
	public String exchange(final String subjectToken, final String scope) {
		MultiValueMap<String, String> formMultiValueData = new LinkedMultiValueMap<>();
		formMultiValueData.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
		formMultiValueData.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
		formMultiValueData.add("client_assertion", clientAssertion());
		formMultiValueData.add("subject_token_type", "urn:ietf:params:oauth:token-type:jwt");
		formMultiValueData.add("subject_token", subjectToken);
		formMultiValueData.add("audience", scope);

		String responseJson = webClient.post()
				.body(BodyInserters.fromFormData(formMultiValueData))
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();

		try {
			return objectMapper.readValue(responseJson, TokenResponse.class).accessToken();
		} catch (JsonProcessingException e) {
			throw new TokenException(format("Klarte ikke parse token fra Azure. Feilmelding=%s", e.getMessage()), e);
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new TokenException(
					format("Klarte ikke hente token fra Tokendings. Feilet med statuskode=%s Feilmelding=%s",
							response.getStatusCode().value(),
							response.getMessage()),
					error);
		} else {
			throw new TokenTechnicalException(
					format("Kall mot Tokendings feilet med feilmelding=%s", error.getMessage()),
					error);
		}
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

	private static String hashedCacheKey(String token, String scope) {
		return sha256Hex(token + scope);
	}
}
