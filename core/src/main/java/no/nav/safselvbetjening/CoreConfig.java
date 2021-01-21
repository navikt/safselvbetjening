package no.nav.safselvbetjening;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@ComponentScan
@EnableRetry
@Configuration
public class CoreConfig {
}
