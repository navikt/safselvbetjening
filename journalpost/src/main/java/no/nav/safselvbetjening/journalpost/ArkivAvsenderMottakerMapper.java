package no.nav.safselvbetjening.journalpost;

import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivAvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ArkivAvsenderMottakerMapper {

    private static final Pattern FNR_SIMPLE_REGEX = Pattern.compile("[0-7]\\d{10}");

	AvsenderMottaker map(ArkivAvsenderMottaker arkivAvsenderMottaker) {
		if(arkivAvsenderMottaker == null) {
			return null;
		}
		if (isBlank(arkivAvsenderMottaker.id())) {
			return null;
		}
		return AvsenderMottaker.builder()
				.id(arkivAvsenderMottaker.id())
				.type(mapAvsenderMottakerIdType(arkivAvsenderMottaker.id(), arkivAvsenderMottaker.type()))
				.navn(arkivAvsenderMottaker.navn())
				.build();
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
