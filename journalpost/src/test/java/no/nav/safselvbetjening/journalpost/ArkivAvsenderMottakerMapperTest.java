package no.nav.safselvbetjening.journalpost;

import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivAvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.ORGNR;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.UKJENT;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.safselvbetjening.journalpost.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_NAVN;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivAvsenderMottakerMapperTest {
	private final String UKJENT_MOTTAKER = "Ukjent mottaker";
	private final ArkivAvsenderMottakerMapper mapper = new ArkivAvsenderMottakerMapper();

	@ParameterizedTest
	@MethodSource("blankStrings")
	void shouldMapToNullWhenInputNullOrBlankAndIdTypeNull(final String id) {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

		assertThat(avsenderMottaker).isNull();
	}

	@SuppressWarnings("unused")
	static Stream<String> blankStrings() {
		return Stream.of("", "   ", null);
	}

	@Test
	void shouldMapToNullWhenIdBlank() {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker("", null, null);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

		assertThat(avsenderMottaker).isNull();
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeFNR() {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_ID_TYPE, AVSENDER_MOTTAKER_NAVN);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

		assertAvsenderMottaker(avsenderMottaker, FNR, AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_NAVN);
	}

	@Nested
	@DisplayName("Test mapping når AvsenderMottakerIdType ikke er satt")
	class AvsenderMottakerIdTypeIsNull {

		@Test
		void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9() {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker("123456789", null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

			assertAvsenderMottaker(avsenderMottaker, ORGNR, "123456789", UKJENT_MOTTAKER);


		}

		@ParameterizedTest
		@ValueSource(strings = {"00000000000", "10000000000", "20000000000", "30000000000",
				"40000000000", "50000000000", "60000000000", "70000000000"})
		void shouldMapAvsenderMottakerIdTypeFNRWhenAvsenderMottakerIdIs11DigitsLongAnd1DigitInRange0To7(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, AVSENDER_MOTTAKER_NAVN);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

			assertAvsenderMottaker(avsenderMottaker, FNR, id, AVSENDER_MOTTAKER_NAVN);
		}

		@ParameterizedTest
		@ValueSource(strings = {"80000000000", "90000000000"})
		@DisplayName("Test mapping av TSS-id")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIs11DigitsLongAndFirstDigitIs8Or9(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

			assertAvsenderMottaker(avsenderMottaker, UKJENT, id, UKJENT_MOTTAKER);
		}

		@ParameterizedTest
		@ValueSource(strings = {"EE:70000000"})
		@DisplayName("Test mapping av referanse til estiske trygdemyndigheter.")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIsLength11AndNonNumeric(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

			assertAvsenderMottaker(avsenderMottaker, UKJENT, id, UKJENT_MOTTAKER);
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345", "1234567890123"})
		void shouldMapAvsenderMottakerIdTypeUKJENT(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker);

			assertAvsenderMottaker(avsenderMottaker, UKJENT, id, UKJENT_MOTTAKER);
		}

	}


	private void assertAvsenderMottaker(AvsenderMottaker avsenderMottaker, AvsenderMottakerIdType code, String id, String navn) {
		assertThat(avsenderMottaker.getType()).isEqualTo(code);
		assertThat(avsenderMottaker.getId()).isEqualTo(id);
		assertThat(avsenderMottaker.getNavn()).isEqualTo(navn);
	}
}