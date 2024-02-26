package no.nav.safselvbetjening.journalpost;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.UtsendingsKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivAvsenderMottaker;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivBruker;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivDokumentinfo;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivFildetaljer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivRelevanteDatoer;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSak;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivSaksrelasjon;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Innsyn;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.Sak;
import no.nav.safselvbetjening.domain.SkjermingType;
import no.nav.safselvbetjening.domain.Variantformat;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.domain.DomainConstants.DOKUMENT_TILGANG_STATUS_OK;
import static no.nav.safselvbetjening.domain.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.safselvbetjening.domain.Kanal.UKJENT;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.Sakstype.fromApplikasjon;
import static no.nav.safselvbetjening.service.Saker.FAGSYSTEM_PENSJON;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ArkivJournalpostMapper {
	static final String FILTYPE_PDFA = "PDFA";
	static final String FILTYPE_PDF = "PDF";
	static final String TILKNYTTET_SOM_HOVEDDOKUMENT = "HOVEDDOKUMENT";
	static final String TILKNYTTET_SOM_VEDLEGG = "VEDLEGG";
	static final Set<String> GYLDIGE_VARIANTER = Set.of("ARKIV", "SLADDET");

	private final ArkivAvsenderMottakerMapper arkivAvsenderMottakerMapper;
	private final UtledTilgangService utledTilgangService;

	public ArkivJournalpostMapper(ArkivAvsenderMottakerMapper arkivAvsenderMottakerMapper,
								  UtledTilgangService utledTilgangService) {
		this.arkivAvsenderMottakerMapper = arkivAvsenderMottakerMapper;
		this.utledTilgangService = utledTilgangService;
	}

	Journalpost map(ArkivJournalpost arkivJournalpost, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		Journalposttype journalposttype = mapJournalposttype(arkivJournalpost);
		Journalpost.TilgangJournalpost tilgang = mapJournalpostTilgang(arkivJournalpost, brukerIdenter, pensjonsakOpt);
		AvsenderMottaker arkivAvsenderMottaker = arkivAvsenderMottakerMapper.map(arkivJournalpost.avsenderMottaker());
		Journalpost journalpost = Journalpost.builder()
				.journalpostId(arkivJournalpost.journalpostId().toString())
				.journalposttype(journalposttype)
				.journalstatus(mapJournalstatus(arkivJournalpost))
				.tema(tilgang.getGjeldendeTema())
				.kanal(mapKanal(arkivJournalpost, journalposttype))
				.tittel(arkivJournalpost.innhold())
				.eksternReferanseId(arkivJournalpost.kanalreferanseId())
				.sak(mapSak(arkivJournalpost.saksrelasjon()))
				.avsender(Journalposttype.I == journalposttype ? arkivAvsenderMottaker : null)
				.mottaker(Journalposttype.U == journalposttype ? arkivAvsenderMottaker : null)
				.relevanteDatoer(mapRelevanteDatoer(arkivJournalpost))
				.dokumenter(mapDokumenter(arkivJournalpost.dokumenter()))
				.tilgang(tilgang)
				.build();
		journalpost.getDokumenter().forEach(dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
				dokumentvariant -> {
					List<String> codes = utledTilgangService.utledTilgangDokument(journalpost, dokumentInfo, dokumentvariant, brukerIdenter);
					dokumentvariant.setBrukerHarTilgang(codes.isEmpty());
					dokumentvariant.setCode(codes.isEmpty() ? singletonList(DOKUMENT_TILGANG_STATUS_OK) : codes);
				}));
		return journalpost;
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

	private static Sak mapSak(ArkivSaksrelasjon arkivSaksrelasjon) {
		if (arkivSaksrelasjon == null) {
			return null;
		}
		if (arkivSaksrelasjon.isPensjonsak()) {
			return Sak.builder()
					.fagsakId(String.valueOf(arkivSaksrelasjon.sakId()))
					.fagsaksystem(FAGSYSTEM_PENSJON)
					.sakstype(FAGSAK)
					.build();
		} else {
			if (arkivSaksrelasjon.fagsystem() == null) {
				return null;
			}
			return Sak.builder()
					.fagsakId(arkivSaksrelasjon.sak().fagsakNr())
					.fagsaksystem(arkivSaksrelasjon.sak().applikasjon())
					.sakstype(fromApplikasjon(arkivSaksrelasjon.sak().applikasjon()))
					.build();
		}
	}

	private static List<RelevantDato> mapRelevanteDatoer(ArkivJournalpost arkivJournalpost) {
		List<RelevantDato> relevanteDatoer = new ArrayList<>();
		ArkivRelevanteDatoer arkivRelevanteDatoer = arkivJournalpost.relevanteDatoer();
		if (arkivRelevanteDatoer.opprettet() != null) {
			relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.opprettet(), Datotype.DATO_OPPRETTET));
		}
		if (arkivRelevanteDatoer.hoveddokument() != null) {
			relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.hoveddokument(), Datotype.DATO_DOKUMENT));
		}
		if (arkivRelevanteDatoer.journalfoert() != null) {
			relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.journalfoert(), Datotype.DATO_JOURNALFOERT));
		}
		switch (JournalpostTypeCode.valueOf(arkivJournalpost.type())) {
			case I:
				if (arkivRelevanteDatoer.forsendelseMottatt() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.forsendelseMottatt(), Datotype.DATO_REGISTRERT));
				}
				break;
			case U:
				if (arkivRelevanteDatoer.sendtPrint() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.sendtPrint(), Datotype.DATO_SENDT_PRINT));
				}
				if (arkivRelevanteDatoer.ekspedert() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.ekspedert(), Datotype.DATO_EKSPEDERT));
				}
				if (arkivRelevanteDatoer.retur() != null) {
					relevanteDatoer.add(new RelevantDato(arkivRelevanteDatoer.retur(), Datotype.DATO_AVS_RETUR));
				}
				break;
			default:
				return relevanteDatoer;
		}
		return relevanteDatoer;
	}

	private List<DokumentInfo> mapDokumenter(List<ArkivDokumentinfo> arkivDokumentinfos) {
		if (arkivDokumentinfos == null || arkivDokumentinfos.isEmpty()) {
			return List.of();
		}

		return arkivDokumentinfos.stream()
				.map(arkivDokumentinfo -> DokumentInfo.builder()
						.dokumentInfoId(String.valueOf(arkivDokumentinfo.dokumentInfoId()))
						.brevkode(arkivDokumentinfo.brevkode())
						.tittel(arkivDokumentinfo.tittel())
						.hoveddokument(TILKNYTTET_SOM_HOVEDDOKUMENT.equals(arkivDokumentinfo.tilknyttetSom()))
						.sensitivtPselv(arkivDokumentinfo.sensitivt())
						.tilgangDokument(DokumentInfo.TilgangDokument.builder()
								.kassert(arkivDokumentinfo.kassert() != null && arkivDokumentinfo.kassert())
								.kategori(arkivDokumentinfo.kategori())
								.build())
						.dokumentvarianter(mapDokumentVarianter(arkivDokumentinfo.fildetaljer()))
						.build()).toList();
	}


	private List<Dokumentvariant> mapDokumentVarianter(List<ArkivFildetaljer> arkivFildetaljer) {
		return arkivFildetaljer.stream()
				.filter(fd -> GYLDIGE_VARIANTER.contains(fd.format()))
				.map(fd -> Dokumentvariant.builder()
						.variantformat(mapVariantformat(fd))
						.filtype(mapFiltype(fd.type()))
						.filuuid(fd.uuid())
						.filstorrelse(mapFilStoerrelse(fd))
						.tilgangVariant(Dokumentvariant.TilgangVariant.builder()
								.skjerming(mapSkjermingType(fd.skjerming()))
								.build())
						.build()).toList();
	}

	private static Variantformat mapVariantformat(ArkivFildetaljer arkivFildetaljer) {
		try {
			return arkivFildetaljer.format() == null ? null : Variantformat.valueOf(arkivFildetaljer.format());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static int mapFilStoerrelse(ArkivFildetaljer fd) {
		if (isBlank(fd.stoerrelse())) {
			return 0;
		} else {
			return parseInt(fd.stoerrelse());
		}
	}

	/**
	 * Filtypen mappet om fra joark sitt domene til safselvbetjening. Konsolidering av PDF/PDFA til PDF.
	 *
	 * @param filtype "PDFA", "PDF", "PNG" osv.
	 * @return "PDFA" mappet om til "PDF". Ellers filtype.
	 */
	private static String mapFiltype(String filtype) {
		if (FILTYPE_PDFA.equals(filtype)) {
			return FILTYPE_PDF;
		}
		return filtype;
	}

	private Journalpost.TilgangJournalpost mapJournalpostTilgang(ArkivJournalpost arkivJournalpost, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		return Journalpost.TilgangJournalpost.builder()
				.journalstatus(arkivJournalpost.status() == null ? null : arkivJournalpost.status())
				.mottakskanal(mapTilgangMottakskanal(arkivJournalpost.mottakskanal()))
				.tema(arkivJournalpost.fagomraade())
				.avsenderMottakerId(mapAvsenderMottakerId(arkivJournalpost.avsenderMottaker()))
				.datoOpprettet(arkivJournalpost.relevanteDatoer() == null ? null : arkivJournalpost.relevanteDatoer().opprettet().toLocalDateTime())
				.journalfoertDato(mapJournalfoert(arkivJournalpost))
				.skjerming(mapSkjermingType(arkivJournalpost.skjerming()))
				.tilgangBruker(mapTilgangBruker(arkivJournalpost.bruker()))
				.tilgangSak(mapTilgangSak(arkivJournalpost, brukerIdenter, pensjonsakOpt))
				.innsyn(mapInnsyn(arkivJournalpost))
				.build();
	}

	private static LocalDateTime mapJournalfoert(ArkivJournalpost arkivJournalpost) {
		ArkivRelevanteDatoer arkivRelevanteDatoer = arkivJournalpost.relevanteDatoer();
		if (arkivRelevanteDatoer == null) {
			return null;
		}
		if (arkivRelevanteDatoer.journalfoert() == null) {
			return null;
		}
		return arkivRelevanteDatoer.journalfoert().toLocalDateTime();
	}

	private static Innsyn mapInnsyn(ArkivJournalpost arkivJournalpost) {
		try {
			return arkivJournalpost.innsyn() == null ? null : Innsyn.valueOf(arkivJournalpost.innsyn());
		} catch (IllegalArgumentException e) {
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
		} catch (IllegalArgumentException e) {
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
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
