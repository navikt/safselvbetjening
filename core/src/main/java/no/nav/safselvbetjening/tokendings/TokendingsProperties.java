package no.nav.safselvbetjening.tokendings;

import com.nimbusds.jose.jwk.RSAKey;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.text.ParseException;

@Validated
@ConfigurationProperties(prefix = "token.x")
public class TokendingsProperties {
	@NotEmpty
	@Getter
	private String clientId;
	@NotNull
	@Getter
	private RSAKey rsaKey;
	@NotEmpty
	@Getter
	private String tokenEndpoint;

	public TokendingsProperties(String clientId, String privateJwk, String tokenEndpoint) throws ParseException {
		if(privateJwk == null) {
			throw new IllegalArgumentException("token.x.private.jwk er null");
		}
		this.clientId = clientId;
		this.rsaKey = RSAKey.parse(privateJwk);
		this.tokenEndpoint = tokenEndpoint;
	}
}
