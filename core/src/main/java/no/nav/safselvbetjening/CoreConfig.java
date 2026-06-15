package no.nav.safselvbetjening;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneId;

import static io.micrometer.core.instrument.config.MeterFilterReply.ACCEPT;
import static io.micrometer.core.instrument.config.MeterFilterReply.DENY;

@Slf4j
@EnableResilientMethods
@EnableJwtTokenValidation
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
@Configuration
public class CoreConfig {

	public static final Clock SYSTEM_CLOCK = Clock.system(ZoneId.of("Europe/Oslo"));

	@Bean
	MeterFilter meterFilter(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		return new MeterFilter() {
			@Override
			public MeterFilterReply accept(Meter.Id id) {
				// Hindre sak metrikker fra å registreres i prometheus
				if (id.getName().startsWith("http.client.requests")
					&& id.getTag("clientName") != null
					&& id.getTag("clientName").startsWith(URI.create(safSelvbetjeningProperties.getEndpoints().getSak().getUrl()).getHost())) {
					return DENY;
				}
				return ACCEPT;
			}
		};
	}

	@Bean
	UtledTilgangService utledTilgangService(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		return new UtledTilgangService();
	}
}
