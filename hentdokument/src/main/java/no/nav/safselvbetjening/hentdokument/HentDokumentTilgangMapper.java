package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivAvsenderMottaker;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivBruker;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivDokumentinfo;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivFildetaljer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSak;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSaksrelasjon;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Innsyn;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.service.BrukerIdenter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HentDokumentTilgangMapper {

	public Journalpost map(ArkivJournalpost arkivJournalpost, String variantFormat, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		return Journalpost.builder()
				.journalpostId(arkivJournalpost.journalpostId().toString())
				.journalposttype(mapJournalposttype(arkivJournalpost))
				.journalstatus(mapJournalstatus(arkivJournalpost))
				.tilgang(mapJournalpostTilgang(arkivJournalpost, brukerIdenter, pensjonsakOpt))
				.dokumenter(mapDokumenter(arkivJournalpost.dokumenter(), variantFormat))
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

	private List<DokumentInfo> mapDokumenter(List<ArkivDokumentinfo> arkivDokumentinfos, String variantFormat) {
		if (arkivDokumentinfos == null || arkivDokumentinfos.isEmpty()) {
			return List.of();
		}

		return arkivDokumentinfos.stream()
				.map(arkivDokumentinfo -> DokumentInfo.builder()
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.kassert(arkivDokumentinfo.kassert() != null && arkivDokumentinfo.kassert())
								.kategori(arkivDokumentinfo.kategori())
								.build())
						.dokumentvarianter(mapDokumentVarianter(arkivDokumentinfo.fildetaljer(), variantFormat))
						.build())
				.collect(Collectors.toList());
	}


	private List<Dokumentvariant> mapDokumentVarianter(List<ArkivFildetaljer> arkivFildetaljer, String variantFormat) {
		return arkivFildetaljer.stream()
				.filter(fd -> fd.format().equals(variantFormat))
				.map(fd -> Dokumentvariant.builder()
						.tilgangVariant(Dokumentvariant.TilgangVariant.builder()
								.skjerming(mapSkjermingType(fd.skjerming()))
								.build())
						.build()).collect(Collectors.toList());
	}

	private Journalpost.TilgangJournalpost mapJournalpostTilgang(ArkivJournalpost arkivJournalpost, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		return Journalpost.TilgangJournalpost.builder()
				.journalstatus(arkivJournalpost.status() == null ? null : arkivJournalpost.status())
				.mottakskanal(mapTilgangMottakskanal(arkivJournalpost.mottakskanal()))
				.tema(arkivJournalpost.fagomraade())
				.avsenderMottakerId(mapAvsenderMottakerId(arkivJournalpost.avsenderMottaker()))
				.datoOpprettet(arkivJournalpost.relevanteDatoer() == null ? null : arkivJournalpost.relevanteDatoer().opprettet().toLocalDateTime())
				.journalfoertDato(arkivJournalpost.relevanteDatoer() == null ? null : arkivJournalpost.relevanteDatoer().journalfoert().toLocalDateTime())
				.skjerming(mapSkjermingType(arkivJournalpost.skjerming()))
				.tilgangBruker(mapTilgangBruker(arkivJournalpost.bruker()))
				.tilgangSak(mapTilgangSak(arkivJournalpost, brukerIdenter, pensjonsakOpt))
				.innsyn(mapInnsyn(arkivJournalpost))
				.build();
	}

	private static Innsyn mapInnsyn(ArkivJournalpost arkivJournalpost) {
		try {
			return arkivJournalpost.innsyn() == null ? null : Innsyn.valueOf(arkivJournalpost.innsyn());
		} catch (Exception e) {
			return null;
		}
	}

	private static String mapAvsenderMottakerId(ArkivAvsenderMottaker avsenderMottaker) {
		if (avsenderMottaker == null) {
			return null;
		}
		return avsenderMottaker.id();
	}

	private Kanal mapTilgangMottakskanal(String mottakskanal) {
		try {
			return mottakskanal == null ? null : MottaksKanalCode.valueOf(mottakskanal).getSafKanal();
		} catch (Exception e) {
			return null;
		}
	}

	private Journalpost.TilgangBruker mapTilgangBruker(ArkivBruker arkivBruker) {
		if (arkivBruker == null) {
			return null;
		}
		return Journalpost.TilgangBruker.builder().brukerId(arkivBruker.id()).build();
	}

	private Journalpost.TilgangSak mapTilgangSak(ArkivJournalpost arkivJournalpost, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		if (arkivJournalpost.isTilknyttetSak()) {
			ArkivSaksrelasjon arkivSaksrelasjon = arkivJournalpost.saksrelasjon();
			Journalpost.TilgangSak.TilgangSakBuilder tilgangSakBuilder = Journalpost.TilgangSak.builder()
					.foedselsnummer(brukerIdenter.getAktivFolkeregisterident())
					.fagsystem(arkivSaksrelasjon.fagsystem())
					.feilregistrert(arkivSaksrelasjon.feilregistrert() != null && arkivSaksrelasjon.feilregistrert());
			if (arkivSaksrelasjon.isPensjonsak()) {
				return tilgangSakBuilder
						.tema(pensjonsakOpt.map(Pensjonsak::arkivtema).orElse(null))
						.build();
			} else {
				ArkivSak arkivSak = arkivSaksrelasjon.sak();
				return tilgangSakBuilder
						.aktoerId(arkivSak.aktoerId())
						.tema(arkivSak.tema())
						.build();
			}
		} else {
			return null;
		}
	}

	private SkjermingType mapSkjermingType(String skjerming) {
		try {
			return skjerming == null ? null : SkjermingType.valueOf(skjerming);
		} catch (Exception e) {
			return null;
		}
	}
}
