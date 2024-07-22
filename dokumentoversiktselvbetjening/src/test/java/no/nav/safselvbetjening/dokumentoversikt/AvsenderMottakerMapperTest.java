package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode.I;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode.N;
import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode.U;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_NAVN;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.ORG_ID_NR;
import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.ORG_NAVN;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.ORGNR;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.UKJENT;
import static org.assertj.core.api.Assertions.assertThat;

class AvsenderMottakerMapperTest {
	private final AvsenderMottakerMapper mapper = new AvsenderMottakerMapper();

	@ParameterizedTest
	@MethodSource("blankStrings")
	void shouldMapToNullWhenInputNullOrBlankAndIdTypeNull(final String input) {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerId(input);
		journalpostDto.setAvsenderMottakerIdType(null);

		AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

		assertThat(avsenderMottaker).isNull();
	}

	@SuppressWarnings("unused")
	static Stream<String> blankStrings() {
		return Stream.of("", "   ", null);
	}

	@Test
	void shouldMapToNullWhenIdBlank() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerId("");
		journalpostDto.setAvsenderMottakerIdType(null);

		AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

		assertThat(avsenderMottaker).isNull();
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeFNR() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerIdType(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID_TYPE_CODE);
		journalpostDto.setAvsenderMottakerId(AVSENDER_MOTTAKER_ID);

		AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

		assertAvsenderMottaker(avsenderMottaker, FNR, AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_NAVN);
	}


	@ParameterizedTest
	@MethodSource("testProvide")
	void shouldMapAvsenderMottakerNavnNull(JournalpostTypeCode type, String navn) {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerNavn(null);
		journalpostDto.setJournalposttype(type);

		AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

		assertAvsenderMottaker(avsenderMottaker, FNR, AVSENDER_MOTTAKER_ID, navn);
	}

	private static Stream<Arguments> testProvide() {
		return Stream.of(
				Arguments.of(I, "Ukjent avsender"),
				Arguments.of(U, "Ukjent mottaker"),
				Arguments.of(N, "Ukjent avsender/mottaker")
		);
	}

	@Nested
	@DisplayName("Test mapping når AvsenderMottakerIdType ikke er satt")
	class AvsenderMottakerIdTypeIsNull {

		@Test
		void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9() {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(ORG_ID_NR);
			journalpostDto.setAvsenderMottakerNavn(ORG_NAVN);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertAvsenderMottaker(avsenderMottaker, ORGNR, ORG_ID_NR, ORG_NAVN);
		}

		@ParameterizedTest
		@ValueSource(strings = {"00000000000", "10000000000", "20000000000", "30000000000",
				"40000000000", "50000000000", "60000000000", "70000000000"})
		void shouldMapAvsenderMottakerIdTypeFNRWhenAvsenderMottakerIdIs11DigitsLongAnd1DigitInRange0To7(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertAvsenderMottaker(avsenderMottaker, FNR, input, AVSENDER_MOTTAKER_NAVN);
		}

		@ParameterizedTest
		@ValueSource(strings = {"80000000000", "90000000000"})
		@DisplayName("Test mapping av TSS-id")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIs11DigitsLongAndFirstDigitIs8Or9(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);
			assertAvsenderMottaker(avsenderMottaker, UKJENT, input, AVSENDER_MOTTAKER_NAVN);
		}

		@ParameterizedTest
		@ValueSource(strings = {"EE:70000000"})
		@DisplayName("Test mapping av referanse til estiske trygdemyndigheter.")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIsLength11AndNonNumeric(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);
			assertAvsenderMottaker(avsenderMottaker, UKJENT, input, AVSENDER_MOTTAKER_NAVN);
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345", "1234567890123"})
		void shouldMapAvsenderMottakerIdTypeUKJENT(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertAvsenderMottaker(avsenderMottaker, UKJENT, input, AVSENDER_MOTTAKER_NAVN);
		}

		private JournalpostDto buildJournalpostDtoAvsenderMottakerIdTypeNull() {
			JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
			journalpostDto.setAvsenderMottakerIdType(null);
			return journalpostDto;
		}
	}

	private void assertAvsenderMottaker(AvsenderMottaker avsenderMottaker, AvsenderMottakerIdType code, String id, String navn) {
		assertThat(avsenderMottaker.getType()).isEqualTo(code);
		assertThat(avsenderMottaker.getId()).isEqualTo(id);
		assertThat(avsenderMottaker.getNavn()).isEqualTo(navn);
	}
}