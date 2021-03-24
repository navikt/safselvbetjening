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
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.UtledTilgangDokumentoversiktService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.STATUS_OK;

/**
 * Mapper fra fagarkivet sitt domene til safselvbetjening sitt domene.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostMapper {

    private final AvsenderMottakerMapper avsenderMottakerMapper;
    private final UtledTilgangDokumentoversiktService utledTilgangDokumentoversiktService;

    public JournalpostMapper(AvsenderMottakerMapper avsenderMottakerMapper, UtledTilgangDokumentoversiktService utledTilgangDokumentoversiktService) {
        this.avsenderMottakerMapper = avsenderMottakerMapper;
        this.utledTilgangDokumentoversiktService = utledTilgangDokumentoversiktService;
    }

    Journalpost map(JournalpostDto journalpostDto, BrukerIdenter brukerIdenter) {
        return Journalpost.builder()
                .journalpostId(journalpostDto.getJournalpostId().toString())
                .journalposttype(journalpostDto.getJournalposttype().toSafJournalposttype())
                .journalstatus(journalpostDto.getJournalstatus().toSafJournalstatus())
                .tittel(journalpostDto.getInnhold())
                .kanal(mapKanal(journalpostDto))
                .avsenderMottaker(avsenderMottakerMapper.map(journalpostDto))
                .relevanteDatoer(mapRelevanteDatoer(journalpostDto))
                .dokumenter(mapDokumenter(journalpostDto, brukerIdenter))
                .build();
    }

    private List<DokumentInfo> mapDokumenter(JournalpostDto journalpostDto, BrukerIdenter brukerIdenter) {
        List<DokumentInfoDto> dokumenter = journalpostDto.getDokumenter();
        return dokumenter.stream().map(dokument -> DokumentInfo.builder()
                .dokumentInfoId(dokument.getDokumentInfoId())
                .dokumentvarianter(mapDokumentVarianter(dokument, journalpostDto, brukerIdenter))
                .tittel(dokument.getTittel())
                .brevkode(dokument.getBrevkode())
        .build()).collect(Collectors.toList());
    }

    private List<Dokumentvariant> mapDokumentVarianter(DokumentInfoDto dokumentInfoDto, JournalpostDto journalpostDto, BrukerIdenter brukerIdenter) {
        List<VariantDto> varianter = dokumentInfoDto.getVarianter();

        return varianter.stream().map(variantDto -> Dokumentvariant.builder()
                .variantformat(variantDto.getVariantf().getSafVariantformat())
                .filuuid(variantDto.getFiluuid())
                .brukerHarTilgang(hasBrukerTilgang(journalpostDto, dokumentInfoDto, brukerIdenter, variantDto))
                .code(returnFeilmeldingListe(journalpostDto, dokumentInfoDto, brukerIdenter, variantDto))
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

    private boolean hasBrukerTilgang(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto, BrukerIdenter brukerIdenter, VariantDto variantDto){
        return utledTilgangDokumentoversiktService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter, variantDto).isEmpty();
    }

    private List<String> returnFeilmeldingListe(JournalpostDto journalpostDto, DokumentInfoDto dokumentInfoDto, BrukerIdenter brukerIdenter, VariantDto variantDto){
        return utledTilgangDokumentoversiktService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter, variantDto).isEmpty()
                ? Collections.singletonList(STATUS_OK) : utledTilgangDokumentoversiktService.utledTilgangDokument(journalpostDto, dokumentInfoDto, brukerIdenter, variantDto);
    }
}
