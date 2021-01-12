package no.nav.safselvbetjening;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(value = {SafSelvbetjeningProperties.class})
@Import(CoreConfig.class)
public class ApplicationConfig {

}