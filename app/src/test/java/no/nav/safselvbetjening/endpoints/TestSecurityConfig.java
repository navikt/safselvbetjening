package no.nav.safselvbetjening.endpoints;

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("itest")
@EnableMockOAuth2Server
public class TestSecurityConfig {
}
