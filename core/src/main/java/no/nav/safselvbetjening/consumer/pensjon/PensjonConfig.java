package no.nav.safselvbetjening.consumer.pensjon;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class PensjonConfig extends AbstractCxfEndpointConfig {

	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/pensjonSak/v1/Binding";
	private static final QName SERVICE_QNAME = new QName(NAMESPACE, "PensjonSak_v1");
	private static final QName PORT_QNAME = new QName(NAMESPACE, "PensjonSak_v1Port");
	private static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/pensjonSak/v1/Binding.wsdl";

	public PensjonConfig(Bus bus, STSConfig stsConfig) {
		super(bus, stsConfig);
	}

	@Bean
	public PensjonSakV1 pensjonSakV1(SafSelvbetjeningProperties safSelvbetjeningProperties) {
		setWsdlUrl(WSDL_URL);
		setServiceName(SERVICE_QNAME);
		setEndpointName(PORT_QNAME);
		setAdress(safSelvbetjeningProperties.getEndpoints().getPensjon());
		setReceiveTimeout(10000);
		setConnectTimeout(3000);
		addFeature(new WSAddressingFeature());
		PensjonSakV1 pensjonSakV1 = createPort(PensjonSakV1.class);
		configureSTSSamlToken(pensjonSakV1);
		return pensjonSakV1;
	}

}


