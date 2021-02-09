package no.nav.safselvbetjening.dokumentoversikt;

import no.nav.safselvbetjening.consumer.fagarkiv.domain.DokumentInfoDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.VariantDto;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper fra fagarkivet sitt domene til safselvbetjening sitt domene.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostMapper {
    private final AvsenderMottakerMapper avsenderMottakerMapper;

    public JournalpostMapper(AvsenderMottakerMapper avsenderMottakerMapper) {
        this.avsenderMottakerMapper = avsenderMottakerMapper;
    }

    Journalpost map(JournalpostDto journalpostDto) {
        return Journalpost.builder()
                .journalpostId(journalpostDto.getJournalpostId().toString())
                .journalposttype(journalpostDto.getJournalposttype().toSafJournalposttype())
                .journalstatus(journalpostDto.getJournalstatus().toSafJournalstatus())
                .tittel(journalpostDto.getInnhold())
                .kanal(mapKanal(journalpostDto))
                .avsenderMottaker(avsenderMottakerMapper.map(journalpostDto))
                .relevanteDatoer(mapRelevanteDatoer(journalpostDto))
                .dokumenter(mapDokumenter(journalpostDto.getDokumenter()))
                .build();
    }

    private List<DokumentInfo> mapDokumenter(List<DokumentInfoDto> dokumenter) {
        return dokumenter.stream().map(d -> DokumentInfo.builder()
                .dokumentInfoId(d.getDokumentInfoId())
                .dokumentvarianter(mapDokumentVarianter(d.getVarianter()))
                .tittel(d.getTittel())
                .brevkode(d.getBrevkode())
        .build()).collect(Collectors.toList());
    }

    private List<Dokumentvariant> mapDokumentVarianter(List<VariantDto> varianter) {
        return varianter.stream().map(v -> Dokumentvariant.builder()
                .variantformat(v.getVariantf().getSafVariantformat())
                .filuuid(v.getFiluuid())
                // FIXME tilgangskontroll
                .brukerHarTilgang(true)
                .code(Arrays.asList("ok"))
                .build()).collect(Collectors.toList());
    }

    private Kanal mapKanal(JournalpostDto journalpostDto) {
        switch (journalpostDto.getJournalposttype()) {
            case I:
                if (journalpostDto.getMottakskanal() == null) {
                    return Kanal.UKJENT;
                }
                return journalpostDto.getMottakskanal().getSafKanal();
            case U:
                if (journalpostDto.getUtsendingskanal() == null) {
                    return mapManglendeUtsendingskanal(journalpostDto);
                }
                return journalpostDto.getUtsendingskanal().getSafKanal();
            case N:
                return Kanal.INGEN_DISTRIBUSJON;
            default:
                return null;
        }
    }

    private Kanal mapManglendeUtsendingskanal(JournalpostDto journalpostDto) {
        switch (journalpostDto.getJournalstatus()) {
            case FL:
                return Kanal.LOKAL_UTSKRIFT;
            case FS:
                return Kanal.SENTRAL_UTSKRIFT;
            case E:
                return Kanal.SENTRAL_UTSKRIFT;
            default:
                return null;
        }
    }

    private List<RelevantDato> mapRelevanteDatoer(JournalpostDto journalpostDto) {
        List<RelevantDato> relevanteDatoer = new ArrayList<>();
        if (journalpostDto.getDokumentDato() != null) {
            relevanteDatoer.add(new RelevantDato(journalpostDto.getDokumentDato(), Datotype.DATO_DOKUMENT));
        }
        if (journalpostDto.getJournalDato() != null) {
            relevanteDatoer.add(new RelevantDato(journalpostDto.getJournalDato(), Datotype.DATO_JOURNALFOERT));
        }
        switch (journalpostDto.getJournalposttype()) {
            case I:
                if (journalpostDto.getMottattDato() != null) {
                    relevanteDatoer.add(new RelevantDato(journalpostDto.getMottattDato(), Datotype.DATO_REGISTRERT));
                }
                break;
            case U:
                if (journalpostDto.getSendtPrintDato() != null) {
                    relevanteDatoer.add(new RelevantDato(journalpostDto.getSendtPrintDato(), Datotype.DATO_SENDT_PRINT));
                }
                if (journalpostDto.getEkspedertDato() != null) {
                    relevanteDatoer.add(new RelevantDato(journalpostDto.getEkspedertDato(), Datotype.DATO_EKSPEDERT));
                }
                if (journalpostDto.getAvsReturDato() != null) {
                    relevanteDatoer.add(new RelevantDato(journalpostDto.getAvsReturDato(), Datotype.DATO_AVS_RETUR));
                }
                break;
            default:
                return relevanteDatoer;
        }
        return relevanteDatoer;
    }
}
