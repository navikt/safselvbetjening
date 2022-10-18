package no.nav.safselvbetjening;

import no.nav.safselvbetjening.azure.AzureProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(value = {
		SafSelvbetjeningProperties.class,
		AzureProperties.class
})
@Import(CoreConfig.class)
public class ApplicationConfig {
}