package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class ArkivAvsenderMottakerMapper {

	private static final Pattern FNR_SIMPLE_REGEX = Pattern.compile("[0-7]\\d{10}");

	AvsenderMottaker map(ArkivAvsenderMottaker arkivAvsenderMottaker, String journalposttype) {
		if (arkivAvsenderMottaker == null) {
			return null;
		}
		if (isBlank(arkivAvsenderMottaker.id())) {
			return null;
		}
		return AvsenderMottaker.builder()
				.id(arkivAvsenderMottaker.id())
				.type(mapAvsenderMottakerIdType(arkivAvsenderMottaker.id(), arkivAvsenderMottaker.type()))
				.navn(mapAvsenderMottakerNavn(arkivAvsenderMottaker.navn(), journalposttype))
				.build();
	}

	private String mapAvsenderMottakerNavn(String navn, String journalposttype) {
		return switch (journalposttype) {
			case "I" -> isEmpty(navn) ? "Ukjent avsender" : navn;
			case "U" -> isEmpty(navn) ? "Ukjent mottaker" : navn;
			default -> isEmpty(navn) ? "Ukjent avsender/mottaker" : navn;
		};
	}

	private AvsenderMottakerIdType mapAvsenderMottakerIdType(String avsenderMottakerId, String avsenderMottakerIdType) {
		if (avsenderMottakerIdType != null) {
			return switch (avsenderMottakerIdType) {
				case "FNR" -> AvsenderMottakerIdType.FNR;
				case "ORGNR" -> AvsenderMottakerIdType.ORGNR;
				case "HPRNR" -> AvsenderMottakerIdType.HPRNR;
				case "UTL_ORG" -> AvsenderMottakerIdType.UTL_ORG;
				default -> AvsenderMottakerIdType.UKJENT;
			};

		} else {
			if (avsenderMottakerId == null) {
				return AvsenderMottakerIdType.NULL;
			} else {
				switch (avsenderMottakerId.length()) {
					case 11:
						if (FNR_SIMPLE_REGEX.matcher(avsenderMottakerId).matches()) {
							return AvsenderMottakerIdType.FNR;
						} else {
							return AvsenderMottakerIdType.UKJENT;
						}
					case 9:
						return AvsenderMottakerIdType.ORGNR;
					default:
						return AvsenderMottakerIdType.UKJENT;
				}
			}
		}
	}
}
