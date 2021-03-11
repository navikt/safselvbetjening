package no.nav.safselvbetjening;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@ComponentScan
@EnableRetry
@EnableJwtTokenValidation
@Configuration
public class CoreConfig {
}
