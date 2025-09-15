package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode.N;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ArkivAvsenderMottakerMapper {

	private static final Pattern FNR_SIMPLE_REGEX = Pattern.compile("[0-7]\\d{10}");

	AvsenderMottaker map(ArkivAvsenderMottaker arkivAvsenderMottaker, String journalposttype) {
		if (arkivAvsenderMottaker == null) {
			return null;
		}

		if (isNotat(journalposttype)) {
			return null;
		}

		if (isBlank(arkivAvsenderMottaker.id()) && isBlank(arkivAvsenderMottaker.navn())) {
			return null;
		}

		return AvsenderMottaker.builder()
				.id(mapAvsenderMottakerId(arkivAvsenderMottaker))
				.type(mapAvsenderMottakerIdType(arkivAvsenderMottaker.id(), arkivAvsenderMottaker.type()))
				.navn(mapAvsenderMottakerNavn(arkivAvsenderMottaker.navn(), journalposttype))
				.build();
	}

	private static boolean isNotat(String journalposttype) {
		return N.name().equals(journalposttype);
	}

	private static String mapAvsenderMottakerId(ArkivAvsenderMottaker arkivAvsenderMottaker) {
		if (isBlank(arkivAvsenderMottaker.id())) {
			return null;
		}
		return arkivAvsenderMottaker.id();
	}

	private static AvsenderMottakerIdType mapAvsenderMottakerIdType(String avsenderMottakerId, String avsenderMottakerIdType) {
		if (isBlank(avsenderMottakerId)) {
			return null;
		}

		if (avsenderMottakerIdType != null) {
			return switch (avsenderMottakerIdType) {
				case "FNR" -> AvsenderMottakerIdType.FNR;
				case "ORGNR" -> AvsenderMottakerIdType.ORGNR;
				case "HPRNR" -> AvsenderMottakerIdType.HPRNR;
				case "UTL_ORG" -> AvsenderMottakerIdType.UTL_ORG;
				default -> AvsenderMottakerIdType.UKJENT;
			};
		} else {
			return switch (avsenderMottakerId.length()) {
				case 11:
					if (FNR_SIMPLE_REGEX.matcher(avsenderMottakerId).matches()) {
						yield AvsenderMottakerIdType.FNR;
					} else {
						yield AvsenderMottakerIdType.UKJENT;
					}
				case 9:
					yield AvsenderMottakerIdType.ORGNR;
				default:
					yield AvsenderMottakerIdType.UKJENT;
			};
		}
	}

	private static String mapAvsenderMottakerNavn(String navn, String journalposttype) {
		boolean isBlankNavn = isBlank(navn);

		return switch (journalposttype) {
			case "I" -> isBlankNavn ? "Ukjent avsender" : navn;
			case "U" -> isBlankNavn ? "Ukjent mottaker" : navn;
			default -> "Ukjent";
		};
	}
}
