package no.nav.safselvbetjening.consumer.pensjon;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Profile({"nais", "local"})
public class STSConfig {

	private final String samlStsUrl;
	private final SafSelvbetjeningProperties.Serviceuser serviceUser;

	public STSConfig(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		this.serviceUser = safSelvbetjeningProperties.getServiceuser();
		this.samlStsUrl = safSelvbetjeningProperties.getEndpoints().getSamlsts();
	}

	public void configureSTS(Object port) {
		Client client = ClientProxy.getClient(port);
		STSConfigUtil.configureStsRequestSamlToken(client, samlStsUrl, serviceUser.getUsername(), serviceUser.getPassword());
	}

}
