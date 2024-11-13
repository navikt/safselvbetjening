package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.UtsendingsKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivDokumentinfo;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivFildetaljer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static no.nav.safselvbetjening.domain.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.safselvbetjening.domain.Kanal.UKJENT;

@Component
public class HentDokumentTilgangMapper {

	static final String TILKNYTTET_SOM_HOVEDDOKUMENT = "HOVEDDOKUMENT";

	public Journalpost map(ArkivJournalpost arkivJournalpost, long dokumentinfoId, String variantFormat, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		Journalposttype journalposttype = mapJournalposttype(arkivJournalpost);
		return Journalpost.builder()
				.journalpostId(arkivJournalpost.journalpostId().toString())
				.journalposttype(journalposttype)
				.journalstatus(mapJournalstatus(arkivJournalpost))
				.kanal(mapKanal(arkivJournalpost, journalposttype))
				.tilgang(arkivJournalpost.getJournalpostTilgang(brukerIdenter, pensjonsakOpt))
				.dokumenter(mapDokumenter(arkivJournalpost.dokumenter(), dokumentinfoId, variantFormat))
				.build();
	}

	private static Journalposttype mapJournalposttype(ArkivJournalpost arkivJournalpost) {
		try {
			return arkivJournalpost.type() == null ? null : JournalpostTypeCode.valueOf(arkivJournalpost.type()).toSafJournalposttype();
		} catch (Exception e) {
			return null;
		}
	}

	private static Journalstatus mapJournalstatus(ArkivJournalpost arkivJournalpost) {
		try {
			return arkivJournalpost.status() == null ? null : JournalStatusCode.valueOf(arkivJournalpost.status()).toSafJournalstatus();
		} catch (Exception e) {
			return null;
		}
	}

	private static Kanal mapKanal(ArkivJournalpost arkivJournalpost, Journalposttype journalposttype) {
		if (journalposttype == null) {
			return UKJENT;
		}
		return switch (journalposttype) {
			case I:
				if (arkivJournalpost.mottakskanal() == null) {
					yield UKJENT;
				}
				yield MottaksKanalCode.valueOf(arkivJournalpost.mottakskanal()).getSafKanal();
			case U:
				if (arkivJournalpost.utsendingskanal() == null) {
					yield UKJENT;
				}
				yield UtsendingsKanalCode.valueOf(arkivJournalpost.utsendingskanal()).getSafKanal();
			case N:
				yield INGEN_DISTRIBUSJON;
		};
	}

	private List<DokumentInfo> mapDokumenter(List<ArkivDokumentinfo> arkivDokumentinfos, long dokumentInfoId, String variantFormat) {
		if (arkivDokumentinfos == null || arkivDokumentinfos.isEmpty()) {
			return List.of();
		}

		return arkivDokumentinfos.stream()
				.filter(arkivDokumentinfo -> arkivDokumentinfo.dokumentInfoId() == dokumentInfoId)
				.map(arkivDokumentinfo -> DokumentInfo.builder()
						.hoveddokument(TILKNYTTET_SOM_HOVEDDOKUMENT.equals(arkivDokumentinfo.tilknyttetSom()))
						.dokumentvarianter(mapDokumentVarianter(arkivDokumentinfo.fildetaljer(), variantFormat))
						.build()).toList();
	}


	private List<Dokumentvariant> mapDokumentVarianter(List<ArkivFildetaljer> arkivFildetaljer, String variantFormat) {
		return arkivFildetaljer.stream()
				.filter(fd -> fd.format().equals(variantFormat))
				.map(fd -> Dokumentvariant.builder().build()).toList();
	}

}
