package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import no.nav.safselvbetjening.domain.Journalposttype;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID_TYPE;
import static no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_NAVN;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.FNR;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.ORGNR;
import static no.nav.safselvbetjening.domain.AvsenderMottakerIdType.UKJENT;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivAvsenderMottakerMapperTest {
	private final String UKJENT_MOTTAKER = "Ukjent mottaker";
	private final String UKJENT_AVSENDER = "Ukjent avsender";
	private final ArkivAvsenderMottakerMapper mapper = new ArkivAvsenderMottakerMapper();

	@Test
	void shouldMapNullWhenArkivAvsenderMottakerNull() {
		AvsenderMottaker avsenderMottaker = mapper.map(null, Journalposttype.U.name());

		assertThat(avsenderMottaker).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	@NullSource
	void shouldMapNullWhenIdAndTypeNullAndNavnNullOrBlank(String navn) {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(null, null, navn);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

		assertThat(avsenderMottaker).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	@NullSource
	void shouldMapNavnWhenIdNullOrBlank(String id) {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, AVSENDER_MOTTAKER_NAVN);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

		assertAvsenderMottaker(avsenderMottaker, null, null, AVSENDER_MOTTAKER_NAVN);
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeFNR() {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_ID_TYPE, AVSENDER_MOTTAKER_NAVN);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

		assertAvsenderMottaker(avsenderMottaker, FNR, AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_NAVN);
	}

	@Test
	void shouldMapAvsenderMottakerNullWhenNotat() {
		// Uansett om metadata er populert
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_ID_TYPE, AVSENDER_MOTTAKER_NAVN);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.N.name());

		assertThat(avsenderMottaker).isNull();
	}

	@Test
	void shouldMapAvsenderMottakerNullWhenOnlyTypeProvided() {
		ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(null, AVSENDER_MOTTAKER_ID_TYPE, null);

		AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.I.name());

		assertThat(avsenderMottaker).isNull();
	}

	@Nested
	@DisplayName("Test mapping når AvsenderMottakerIdType ikke er satt")
	class AvsenderMottakerIdTypeIsNull {

		@Test
		void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9() {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker("123456789", null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

			assertAvsenderMottaker(avsenderMottaker, ORGNR, "123456789", UKJENT_MOTTAKER);
		}

		@Test
		void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9AndIncomingDocument() {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker("123456789", null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.I.name());

			assertAvsenderMottaker(avsenderMottaker, ORGNR, "123456789", UKJENT_AVSENDER);
		}

		@ParameterizedTest
		@ValueSource(strings = {"00000000000", "10000000000", "20000000000", "30000000000",
				"40000000000", "50000000000", "60000000000", "70000000000"})
		void shouldMapAvsenderMottakerIdTypeFNRWhenAvsenderMottakerIdIs11DigitsLongAnd1DigitInRange0To7(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, AVSENDER_MOTTAKER_NAVN);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

			assertAvsenderMottaker(avsenderMottaker, FNR, id, AVSENDER_MOTTAKER_NAVN);
		}

		@ParameterizedTest
		@ValueSource(strings = {"80000000000", "90000000000"})
		@DisplayName("Test mapping av TSS-id")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIs11DigitsLongAndFirstDigitIs8Or9(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

			assertAvsenderMottaker(avsenderMottaker, UKJENT, id, UKJENT_MOTTAKER);
		}

		@ParameterizedTest
		@ValueSource(strings = {"EE:70000000"})
		@DisplayName("Test mapping av referanse til estiske trygdemyndigheter.")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIsLength11AndNonNumeric(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

			assertAvsenderMottaker(avsenderMottaker, UKJENT, id, UKJENT_MOTTAKER);
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345", "1234567890123"})
		void shouldMapAvsenderMottakerIdTypeUKJENT(String id) {
			ArkivAvsenderMottaker arkivAvsenderMottaker = new ArkivAvsenderMottaker(id, null, null);

			AvsenderMottaker avsenderMottaker = mapper.map(arkivAvsenderMottaker, Journalposttype.U.name());

			assertAvsenderMottaker(avsenderMottaker, UKJENT, id, UKJENT_MOTTAKER);
		}

	}

	private void assertAvsenderMottaker(AvsenderMottaker avsenderMottaker, AvsenderMottakerIdType type, String id, String navn) {
		assertThat(avsenderMottaker.getType()).isEqualTo(type);
		assertThat(avsenderMottaker.getId()).isEqualTo(id);
		assertThat(avsenderMottaker.getNavn()).isEqualTo(navn);
	}
}