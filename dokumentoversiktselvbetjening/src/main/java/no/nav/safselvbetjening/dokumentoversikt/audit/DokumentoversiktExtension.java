package no.nav.safselvbetjening.dokumentoversikt.audit;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.audit.cef.Extension;

@Slf4j
@SuperBuilder
public class DokumentoversiktExtension extends Extension {

	@Override
	protected String getDeviceCustomStringsCef() {
		return "";
	}
}
