package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.JournalpostTypeCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.MottaksKanalCode;
import no.nav.safselvbetjening.consumer.dokarkiv.domain.UtsendingsKanalCode;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.AvsenderMottaker;
import no.nav.safselvbetjening.domain.Datotype;
import no.nav.safselvbetjening.domain.DokumentInfo;
import no.nav.safselvbetjening.domain.Dokumentvariant;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.domain.Journalposttype;
import no.nav.safselvbetjening.domain.Journalstatus;
import no.nav.safselvbetjening.domain.Kanal;
import no.nav.safselvbetjening.domain.RelevantDato;
import no.nav.safselvbetjening.domain.Sak;
import no.nav.safselvbetjening.domain.Variantformat;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.tilgang.TilgangDenyReason;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.domain.DomainConstants.DOKUMENT_TILGANG_STATUS_OK;
import static no.nav.safselvbetjening.domain.Kanal.INGEN_DISTRIBUSJON;
import static no.nav.safselvbetjening.domain.Kanal.UKJENT;
import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;
import static no.nav.safselvbetjening.domain.Sakstype.fromApplikasjon;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ArkivJournalpostMapper {
	public static final String FAGSYSTEM_PENSJON = "PP01";
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

	public Journalpost map(ArkivJournalpost arkivJournalpost, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		Journalposttype journalposttype = mapJournalposttype(arkivJournalpost);
		TilgangJournalpost tilgang = mapJournalpostTilgang(arkivJournalpost, brukerIdenter, pensjonsakOpt);

		Map<Long, Map<TilgangVariantFormat, List<TilgangDenyReason>>> dokumentTilganger = new HashMap<>();
		for (TilgangDokument tilgangDokument : tilgang.getDokumenter()) {
			Map<TilgangVariantFormat, List<TilgangDenyReason>> variantTilganger = new HashMap<>();
			for (TilgangVariant variant : tilgangDokument.dokumentvarianter()) {
				List<TilgangDenyReason> denyReasons = utledTilgangService.utledTilgangDokument(tilgang, tilgangDokument, variant, brukerIdenter.getIdenter());
				variantTilganger.put(variant.variantformat(), denyReasons);
			}
			dokumentTilganger.put(tilgangDokument.id(), variantTilganger);
		}

		AvsenderMottaker arkivAvsenderMottaker = arkivAvsenderMottakerMapper.map(arkivJournalpost.avsenderMottaker(), arkivJournalpost.type());
		return Journalpost.builder()
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
				.dokumenter(mapDokumenter(arkivJournalpost.dokumenter(), dokumentTilganger))
				.tilgang(tilgang)
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

	private List<DokumentInfo> mapDokumenter(List<ArkivDokumentinfo> arkivDokumentinfos, Map<Long, Map<TilgangVariantFormat, List<TilgangDenyReason>>> dokumentTilganger) {
		if (arkivDokumentinfos == null || arkivDokumentinfos.isEmpty()) {
			return emptyList();
		}

		return arkivDokumentinfos.stream()
				.map(arkivDokumentinfo -> DokumentInfo.builder()
						.dokumentInfoId(String.valueOf(arkivDokumentinfo.dokumentInfoId()))
						.brevkode(arkivDokumentinfo.brevkode())
						.tittel(arkivDokumentinfo.tittel())
						.hoveddokument(TILKNYTTET_SOM_HOVEDDOKUMENT.equals(arkivDokumentinfo.tilknyttetSom()))
						.sensitivtPselv(arkivDokumentinfo.sensitivt())
						.dokumentvarianter(mapDokumentVarianter(arkivDokumentinfo.fildetaljer(), dokumentTilganger.get(arkivDokumentinfo.dokumentInfoId())))
						.build())
				.toList();
	}


	private List<Dokumentvariant> mapDokumentVarianter(List<ArkivFildetaljer> arkivFildetaljer, Map<TilgangVariantFormat, List<TilgangDenyReason>> variantTilganger) {
		return arkivFildetaljer.stream()
				.filter(fildetaljer -> GYLDIGE_VARIANTER.contains(fildetaljer.format()))
				.map(fildetaljer -> {
					TilgangVariantFormat tilgangVariantFormat = TilgangVariantFormat.from(fildetaljer.format());
					return Dokumentvariant.builder()
							.variantformat(mapVariantformat(fildetaljer))
							.filtype(mapFiltype(fildetaljer.type()))
							.filuuid(fildetaljer.uuid())
							.filstorrelse(mapFilStoerrelse(fildetaljer))
							.brukerHarTilgang(variantTilganger.get(tilgangVariantFormat).isEmpty())
							.code(mapTilgangForVariant(variantTilganger, tilgangVariantFormat))
							.build();
				})
				.toList();
	}

	private static List<String> mapTilgangForVariant(Map<TilgangVariantFormat, List<TilgangDenyReason>> variantTilganger, TilgangVariantFormat variantFormat) {
		if (variantTilganger.get(variantFormat).isEmpty()) {
			return singletonList(DOKUMENT_TILGANG_STATUS_OK);
		}
		return variantTilganger.get(variantFormat).stream().map(denyReason -> denyReason.reason).toList();
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

	private TilgangJournalpost mapJournalpostTilgang(ArkivJournalpost arkivJournalpost, BrukerIdenter brukerIdenter, Optional<Pensjonsak> pensjonsakOpt) {
		return arkivJournalpost.getJournalpostTilgang(brukerIdenter, pensjonsakOpt);
	}

}
