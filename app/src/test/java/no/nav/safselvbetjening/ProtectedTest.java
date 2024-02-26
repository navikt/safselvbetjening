package no.nav.safselvbetjening;

import no.nav.safselvbetjening.graphql.GraphQLController;
import no.nav.safselvbetjening.rest.HentDokumentController;
import no.nav.security.token.support.core.api.Protected;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtectedTest {

	@Test
	void skalSikreAtProtectedAnnotasjonErPaaGraphqlController() {
		assertThat(GraphQLController.class.isAnnotationPresent(Protected.class)).isTrue();
	}

	@Test
	void skalSikreAtProtectedAnnotasjonErPaaHentDokumentController() {
		assertThat(HentDokumentController.class.isAnnotationPresent(Protected.class)).isTrue();
	}
}
