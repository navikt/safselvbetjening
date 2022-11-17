package no.nav.safselvbetjening.dokumentoversikt;


import no.nav.safselvbetjening.consumer.fagarkiv.domain.AvsenderMottakerIdTypeCode;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.AvsenderMottakerIdType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class AvsenderMottakerMapper {
    private static final Pattern FNR_SIMPLE_REGEX = Pattern.compile("[0-7]\\d{10}");

    AvsenderMottaker map(JournalpostDto journalpostDto) {
        if(isBlank(journalpostDto.getAvsenderMottakerId())) {
            return null;
        }
        return AvsenderMottaker.builder()
                .id(journalpostDto.getAvsenderMottakerId())
                .type(mapAvsenderMottakerIdType(journalpostDto.getAvsenderMottakerId(), journalpostDto.getAvsenderMottakerIdType()))
                .build();
    }

    private AvsenderMottakerIdType mapAvsenderMottakerIdType(String avsenderMottakerId, AvsenderMottakerIdTypeCode avsenderMottakerIdTypeCode) {
        if (avsenderMottakerIdTypeCode != null) {
            return switch (avsenderMottakerIdTypeCode) {
                case FNR -> AvsenderMottakerIdType.FNR;
                case ORGNR -> AvsenderMottakerIdType.ORGNR;
                case HPRNR -> AvsenderMottakerIdType.HPRNR;
                case UTL_ORG -> AvsenderMottakerIdType.UTL_ORG;
                default -> AvsenderMottakerIdType.UKJENT;
            };

        } else {
            if (avsenderMottakerId == null) {
                return AvsenderMottakerIdType.NULL;
            } else {
                switch (avsenderMottakerId.length()) {
                    case 11:
                        if(FNR_SIMPLE_REGEX.matcher(avsenderMottakerId).matches()) {
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
