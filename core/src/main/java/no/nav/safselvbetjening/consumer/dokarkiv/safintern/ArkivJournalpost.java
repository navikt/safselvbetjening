package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.AktoerId;
import no.nav.safselvbetjening.tilgang.Foedselsnummer;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.Organisasjonsnummer;
import no.nav.safselvbetjening.tilgang.TilgangBruker;
import no.nav.safselvbetjening.tilgang.TilgangGosysSak;
import no.nav.safselvbetjening.tilgang.TilgangInnsyn;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangJournalposttype;
import no.nav.safselvbetjening.tilgang.TilgangJournalstatus;
import no.nav.safselvbetjening.tilgang.TilgangMottakskanal;
import no.nav.safselvbetjening.tilgang.TilgangPensjonSak;
import no.nav.safselvbetjening.tilgang.TilgangSak;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNumeric;

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

	private Ident mapAvsenderMottakerId() {
		if (avsenderMottaker == null || avsenderMottaker.id() == null) {
			return null;
		}

		if (avsenderMottaker.type() != null) {
			if ("ORGNR".equalsIgnoreCase(avsenderMottaker.type())) {
				return Organisasjonsnummer.of(avsenderMottaker.id());
			} else if ("FNR".equalsIgnoreCase(avsenderMottaker.type())) {
				return Foedselsnummer.of(avsenderMottaker.id());
			} else {
				return AktoerId.of(avsenderMottaker.id());
			}
		} else {
			switch (avsenderMottaker.id().length()) {
				case 11:
					if (isNumeric(avsenderMottaker.id())) {
						return Foedselsnummer.of(avsenderMottaker.id());
					} else {
						return AktoerId.of(avsenderMottaker.id());
					}
				case 9:
					return Organisasjonsnummer.of(avsenderMottaker.id());
				default:
					return AktoerId.of(avsenderMottaker.id());
			}

		}
	}

	private TilgangBruker mapTilgangBruker() {
		if (bruker == null || bruker.id() == null) {
			return null;
		}

		if ("ORGANISASJON".equals(bruker.type())) {
			return new TilgangBruker(Organisasjonsnummer.of(bruker.id()));
		} else if ("PERSON".equalsIgnoreCase(bruker.type())) {
			return new TilgangBruker(Foedselsnummer.of(bruker.id()));
		} else {
			return new TilgangBruker(AktoerId.of(bruker.id()));
		}
	}

	private TilgangSak mapTilgangSak(BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		if (!isTilknyttetSak()) {
			return null;
		}

		if (saksrelasjon.isPensjonsak()) {
			return TilgangPensjonSak.builder()
					.feilregistrert(saksrelasjon.feilregistrert() != null && saksrelasjon.feilregistrert())
					.tema(pensjonsakOpt.map(Pensjonsak::arkivtema).orElse(null))
					.foedselsnummer(Foedselsnummer.of(brukerIdenter.getAktivFolkeregisterident()))
					.build();
		} else {
			ArkivSak arkivSak = saksrelasjon.sak();
			return TilgangGosysSak.builder()
					.feilregistrert(saksrelasjon.feilregistrert() != null && saksrelasjon.feilregistrert())
					.tema(arkivSak.tema())
					.aktoerId(AktoerId.ofNullable(arkivSak.aktoerId()))
					.build();
		}
	}

	private TilgangSkjermingType mapSkjermingType() {
		return TilgangSkjermingType.from(skjerming);
	}
}
