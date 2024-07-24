package no.nav.safselvbetjening.tokendings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenResponse(
		@JsonProperty(value = "access_token", required = true) String accessToken,
		@JsonProperty(value = "expires_in", required = true) long expiresIn
){}
