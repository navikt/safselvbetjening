package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.AktoerId;
import no.nav.safselvbetjening.tilgang.Foedselsnummer;
import no.nav.safselvbetjening.tilgang.Organisasjonsnummer;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangFagsystem;
import no.nav.safselvbetjening.tilgang.TilgangInnsyn;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalposttype;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangMottakskanal;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

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

	private String mapAvsenderMottakerId() {
		if (avsenderMottaker == null) {
			return null;
		}

		return avsenderMottaker.id();
	}

	private TilgangBruker mapTilgangBruker() {
		if (bruker == null) {
			return null;
		}
		if ("ORGANISASJON".equals(bruker.type())) {
			return new TilgangBruker(Organisasjonsnummer.of(bruker.id()));
		}

		return new TilgangBruker(Foedselsnummer.of(bruker.id()));
	}

	private TilgangSak mapTilgangSak(BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		if (!this.isTilknyttetSak()) {
			return null;
		}

		ArkivSaksrelasjon arkivSaksrelasjon = this.saksrelasjon();
		TilgangSak.TilgangSakBuilder tilgangSakBuilder = TilgangSak.builder()
				.foedselsnummer(Foedselsnummer.of(brukerIdenter.getAktivFolkeregisterident()))
				.fagsystem(TilgangFagsystem.from(arkivSaksrelasjon.fagsystem()))
				.feilregistrert(arkivSaksrelasjon.feilregistrert() != null && arkivSaksrelasjon.feilregistrert());

		if (arkivSaksrelasjon.isPensjonsak()) {
			return tilgangSakBuilder
					.tema(pensjonsakOpt.map(Pensjonsak::arkivtema).orElse(null))
					.build();
		} else {
			ArkivSak arkivSak = arkivSaksrelasjon.sak();
			return tilgangSakBuilder
					.aktoerId(arkivSak.aktoerId() != null ? AktoerId.of(arkivSak.aktoerId()) : null)
					.tema(arkivSak.tema())
					.build();
		}
	}

	private TilgangSkjermingType mapSkjermingType() {
		return TilgangSkjermingType.from(skjerming);
	}
}
