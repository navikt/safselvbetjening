package no.nav.safselvbetjening.endpoints;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.pensjon.STSConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("itest")
public class STSTestConfig extends STSConfig {

	public STSTestConfig(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		super(safSelvbetjeningProperties);
	}

	@Override
	public void configureSTS(Object port) {
	}
}
