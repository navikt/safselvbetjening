package no.nav.safselvbetjening.service;

import no.nav.safselvbetjening.consumer.pdl.PdlResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrukerIdenterTest {

	@Test
	void shouldReturnAnonymousToString() {
		PdlResponse.PdlIdent aktoerId = new PdlResponse.PdlIdent();
		aktoerId.setIdent("1111111111111");
		aktoerId.setGruppe(PdlResponse.PdlGruppe.AKTORID);
		aktoerId.setHistorisk(false);

		PdlResponse.PdlIdent fnr = new PdlResponse.PdlIdent();
		fnr.setIdent("11111111111");
		fnr.setGruppe(PdlResponse.PdlGruppe.FOLKEREGISTERIDENT);
		fnr.setHistorisk(false);

		List<PdlResponse.PdlIdent> identer = List.of(aktoerId, fnr);
		assertThat(new BrukerIdenter(identer).toString()).isEqualTo("BrukerIdenter{antallAktoerIds=1, antallFoedselsnummer=1}");
	}
}