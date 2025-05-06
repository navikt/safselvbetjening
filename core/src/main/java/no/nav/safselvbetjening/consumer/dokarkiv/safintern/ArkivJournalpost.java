package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangInnsyn;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalposttype;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangMottakskanal;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;
import no.nav.safselvbetjening.tilgang.TilgangUtsendingskanal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Builder
public record ArkivJournalpost(Long journalpostId,
							   String type,
							   String fagomraade,
							   String status,
							   String mottakskanal,
							   String utsendingskanal,
							   String innsyn,
							   String skjerming,
							   ArkivRelevanteDatoer relevanteDatoer,
							   ArkivAvsenderMottaker avsenderMottaker,
							   ArkivBruker bruker,
							   ArkivSaksrelasjon saksrelasjon,
							   List<ArkivDokumentinfo> dokumenter,
							   // kun metadata, ikke brukt til tilgangskontroll
							   String innhold,
							   String kanalreferanseId

) {
	public boolean isTilknyttetSak() {
		return saksrelasjon != null && saksrelasjon.sakId() != null;
	}

	public TilgangJournalpost getJournalpostTilgang(BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		return TilgangJournalpost.builder()
				.journalstatus(TilgangJournalstatus.from(status))
				.journalposttype(TilgangJournalposttype.from(type))
				.mottakskanal(TilgangMottakskanal.from(mottakskanal))
				.utsendingskanal(TilgangUtsendingskanal.from(utsendingskanal))
				.tema(fagomraade)
				.avsenderMottakerId(mapAvsenderMottakerId())
				.datoOpprettet(relevanteDatoer == null ? LocalDateTime.MIN : relevanteDatoer.opprettet().toLocalDateTime())
				.journalfoertDato(mapJournalfoert())
				.skjerming(mapSkjermingType())
				.dokumenter(dokumenter == null ? emptyList() : dokumenter.stream().map(ArkivDokumentinfo::getTilgangDokument).toList())
				.tilgangBruker(mapTilgangBruker())
				.tilgangSak(mapTilgangSak(brukerIdenter, pensjonsakOpt))
				.innsyn(TilgangInnsyn.from(innsyn))
				.build();
	}

	private LocalDateTime mapJournalfoert() {
		if (relevanteDatoer == null || relevanteDatoer.journalfoert() == null) {
			return null;
		}
		return relevanteDatoer.journalfoert().toLocalDateTime();
	}

	private Ident mapAvsenderMottakerId() {
		if (avsenderMottaker == null || avsenderMottaker.id() == null) {
			return null;
		}
		return Ident.ofNullable(avsenderMottaker.id());
	}

	private TilgangBruker mapTilgangBruker() {
		if (bruker == null || bruker.id() == null) {
			return null;
		}

		return new TilgangBruker(Ident.of(bruker.id()));
	}

	private TilgangSak mapTilgangSak(BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		if (!isTilknyttetSak()) {
			return null;
		}

		if (saksrelasjon.isPensjonsak()) {
			return TilgangSak.builder()
					.feilregistrert(saksrelasjon.feilregistrert() != null && saksrelasjon.feilregistrert())
					.tema(pensjonsakOpt.map(Pensjonsak::arkivtema).orElse(null))
					.ident(Ident.of(brukerIdenter.getAktivFolkeregisterident()))
					.build();
		} else {
			ArkivSak arkivSak = saksrelasjon.sak();
			return TilgangSak.builder()
					.feilregistrert(saksrelasjon.feilregistrert() != null && saksrelasjon.feilregistrert())
					.tema(arkivSak.tema())
					.ident(Ident.ofNullable(arkivSak.aktoerId()))
					.build();
		}
	}

	private TilgangSkjermingType mapSkjermingType() {
		return TilgangSkjermingType.from(skjerming);
	}
}
