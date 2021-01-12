package no.nav.safselvbetjening;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {SafSelvbetjeningProperties.class})
public class ApplicationConfig {

}