package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.safselvbetjening.dokumentoversikt.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class AvsenderMottakerMapperTest {
	private final AvsenderMottakerMapper mapper = new AvsenderMottakerMapper();

	@Test
	void shouldMapAvsenderMottakerIdTypeNull() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerId(null);
		journalpostDto.setAvsenderMottakerIdType(null);

		AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

		assertThat(avsenderMottaker.getId()).isNull();
		assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.NULL);
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeFNR() {
		JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
		journalpostDto.setAvsenderMottakerIdType(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID_TYPE_CODE);
		journalpostDto.setAvsenderMottakerId(AVSENDER_MOTTAKER_ID);

		AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

		assertThat(avsenderMottaker.getId()).isEqualTo(AVSENDER_MOTTAKER_ID);
		assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.FNR);
	}

	@Nested
	@DisplayName("Test mapping når AvsenderMottakerIdType ikke er satt")
	class AvsenderMottakerIdTypeIsNull {

		@Test
		void shouldMapAvsenderMottakerIdTypeNullWhenAvsenderMottakerIdIsNull() {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(null);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.NULL);
		}

		@Test
		void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9() {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId("123456789");

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.ORGNR);
		}

		@ParameterizedTest
		@ValueSource(strings = {"00000000000", "10000000000", "20000000000", "30000000000",
				"40000000000", "50000000000", "60000000000", "70000000000"})
		void shouldMapAvsenderMottakerIdTypeFNRWhenAvsenderMottakerIdIs11DigitsLongAnd1DigitInRange0To7(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.FNR);
		}

		@ParameterizedTest
		@ValueSource(strings = {"80000000000", "90000000000"})
		@DisplayName("Test mapping av TSS-id")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIs11DigitsLongAndFirstDigitIs8Or9(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
		}

		@ParameterizedTest
		@ValueSource(strings = {"EE:70000000"})
		@DisplayName("Test mapping av referanse til estiske trygdemyndigheter.")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIsLength11AndNonNumeric(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345", "1234567890123", ""})
		void shouldMapAvsenderMottakerIdTypeUKJENT(String input) {
			JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
			journalpostDto.setAvsenderMottakerId(input);

			AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
		}

		private JournalpostDto buildJournalpostDtoAvsenderMottakerIdTypeNull() {
			JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
			journalpostDto.setAvsenderMottakerIdType(null);
			return journalpostDto;
		}
	}
}