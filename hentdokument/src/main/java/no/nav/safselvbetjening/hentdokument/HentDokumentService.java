package no.nav.safselvbetjening.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.dokarkiv.DokarkivConsumer;
import no.nav.safselvbetjening.consumer.dokarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.dokarkiv.safintern.ArkivJournalpost;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakRestConsumer;
import no.nav.safselvbetjening.consumer.pensjon.Pensjonsak;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.hentdokument.audit.HentDokumentAudit;
import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.FullmaktInvalidException;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.safselvbetjening.tilgang.Ident;
import no.nav.safselvbetjening.tilgang.NoValidTokensException;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangJournalpost;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;
import no.nav.safselvbetjening.tilgang.TilgangsvalideringService;
import no.nav.safselvbetjening.tilgang.UserNotMatchingTokenException;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static no.nav.safselvbetjening.CoreConfig.SYSTEM_CLOCK;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_INGEN_GYLDIG_TOKEN;
import static no.nav.safselvbetjening.DenyReasonFactory.lagFeilmeldingForDokument;
import static no.nav.safselvbetjening.DenyReasonFactory.lagFeilmeldingForJournalpost;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_FOER_INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_IKKE_AVSENDER_MOTTAKER;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_UGYLDIG_VARIANTFORMAT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class HentDokumentService {
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	public static final String DENY_REASON_INGEN_GYLDIG_TOKEN = "ingen_gyldig_token";
	public static final String DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN = "bruker_matcher_ikke_token";
	public static final String DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA = "fullmakt_gjelder_ikke_tema";
	public static final Set<String> HENTDOKUMENT_TILGANG_FIELDS = Set.of(
			"journalpostId", "fagomraade", "status", "type", "skjerming", "mottakskanal", "utsendingskanal", "innsyn",
			"bruker", "avsenderMottaker", "relevanteDatoer", "saksrelasjon",
			"dokumenter.dokumentInfoId", "dokumenter.tilknyttetSom", "dokumenter.kassert", "dokumenter.kategori", "dokumenter.skjerming", "dokumenter.fildetaljer");

	private final DokarkivConsumer dokarkivConsumer;
	private final IdentService identService;
	private final UtledTilgangService utledTilgangService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final HentDokumentTilgangMapper hentDokumentTilgangMapper;
	private final KafkaEventProducer kafkaProducer;
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final HentDokumentAudit audit;
	private final TilgangsvalideringService tilgangsvalideringService;

	public HentDokumentService(
			DokarkivConsumer dokarkivConsumer,
			IdentService identService,
			TilgangsvalideringService tilgangsvalideringService,
			UtledTilgangService utledTilgangService,
			PensjonSakRestConsumer pensjonSakRestConsumer,
			HentDokumentTilgangMapper hentDokumentTilgangMapper,
			KafkaEventProducer kafkaProducer,
			SafSelvbetjeningProperties safSelvbetjeningProperties
	) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.identService = identService;
		this.tilgangsvalideringService = tilgangsvalideringService;
		this.utledTilgangService = utledTilgangService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.hentDokumentTilgangMapper = hentDokumentTilgangMapper;
		this.kafkaProducer = kafkaProducer;
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
		this.audit = new HentDokumentAudit(SYSTEM_CLOCK);
	}

	public HentDokument hentDokument(final HentdokumentRequest hentdokumentRequest) {
		Tilgangskontroll tilgangskontroll = doTilgangskontroll(hentdokumentRequest);

		final HentDokumentResponseTo hentDokumentResponseTo = dokarkivConsumer.hentDokument(
				hentdokumentRequest.getDokumentInfoId(),
				tilgangskontroll.determinedVariantFormat()
		);

		if (tilgangskontroll.genererHoveddokumentLestHendelse()) {
			sendHoveddokumentLestHendelse(hentdokumentRequest);
		}

		return HentDokument.builder()
				.dokument(hentDokumentResponseTo.getDokument())
				.mediaType(hentDokumentResponseTo.getMediaType())
				.variantformat(tilgangskontroll.determinedVariantFormat().name())
				.extension(MimetypeFileextensionMapper.toFileextension(hentDokumentResponseTo.getMediaType()))
				.build();
	}

	private Tilgangskontroll doTilgangskontroll(final HentdokumentRequest hentdokumentRequest) {
		ArkivJournalpost arkivJournalpost = dokarkivConsumer.journalpost(hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), HENTDOKUMENT_TILGANG_FIELDS);
		validerRiktigJournalpost(hentdokumentRequest, arkivJournalpost);
		final BrukerIdenter brukerIdenter = identService.hentIdenter(arkivJournalpost);
		if (brukerIdenter.isEmpty()) {
			throw new HentTilgangDokumentException(DENY_REASON_IKKE_AVSENDER_MOTTAKER.reason, FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
		}

		try {
			Optional<Fullmakt> fullmaktOptional = tilgangsvalideringService.validerInnloggetBrukerOgFinnFullmakt(brukerIdenter,
					hentdokumentRequest.getTokenValidationContext());
			Optional<Pensjonsak> pensjonsakOpt = hentPensjonssak(brukerIdenter.getAktivFolkeregisterident(), arkivJournalpost, fullmaktOptional);
			Journalpost journalpost = hentDokumentTilgangMapper.map(arkivJournalpost, Long.parseLong(hentdokumentRequest.getDokumentInfoId()),
					hentdokumentRequest.getVariantFormat(), brukerIdenter, pensjonsakOpt);
			String gjeldendeTema = journalpost.getTilgang().getGjeldendeTema();
			fullmaktOptional.ifPresent(fullmakt -> {
				TilgangsvalideringService.validerFullmaktForTema(fullmakt, gjeldendeTema,
						fullmaktPresentAndValidAuditLog(hentdokumentRequest, gjeldendeTema)
				);
			});

			TilgangJournalpost tilgangJournalpost = journalpost.getTilgang();
			TilgangVariantFormat variantFormat = utledTilgangHentDokument(tilgangJournalpost, brukerIdenter.getIdenter(), Long.parseLong(hentdokumentRequest.getDokumentInfoId()), hentdokumentRequest.getVariantFormat());
			recordFullmaktAuditLog(fullmaktOptional, hentdokumentRequest);

			return new Tilgangskontroll(journalpost, variantFormat, fullmaktOptional);
		} catch (NoValidTokensException e) {
			throw new HentTilgangDokumentException(DENY_REASON_INGEN_GYLDIG_TOKEN, FEILMELDING_INGEN_GYLDIG_TOKEN);
		} catch (UserNotMatchingTokenException e) {
			secureLog.warn("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}) Innlogget bruker med ident={} matcher ikke bruker på journalpost og har ingen fullmakt. brukerIdenter={}",
					hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(),
					e.getIdent(), e.getIdenter());
			throw new HentTilgangDokumentException(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
		} catch (FullmaktInvalidException e) {
			secureLog.warn("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}, tema={}) Innlogget bruker med ident={} har fullmakt som ikke dekker tema for dokument tilhørende bruker={}. Tilgang er avvist",
					hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(), e.getGjeldendeTema(),
					e.getFullmakt().fullmektig(), e.getFullmakt().fullmaktsgiver());
			throw new HentTilgangDokumentException(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA, FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
		}
	}

	private static Consumer<Fullmakt> fullmaktPresentAndValidAuditLog(HentdokumentRequest hentdokumentRequest, String gjeldendeTema) {
		return fullmakt -> {
			secureLog.info("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}, tema={}) Innlogget bruker med ident={} bruker fullmakt med tema={} for dokument tilhørende bruker={}",
					hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(), gjeldendeTema,
					fullmakt.fullmektig(), fullmakt.tema(), fullmakt.fullmaktsgiver());
		};
	}

	public void recordFullmaktAuditLog(Optional<Fullmakt> fullmaktOpt, HentdokumentRequest hentdokumentRequest) {
		fullmaktOpt.ifPresentOrElse(fullmakt -> audit.logSomFullmektig(fullmakt, hentdokumentRequest),
				() -> audit.logSomBruker(hentdokumentRequest, tilgangsvalideringService.getPidOrSubFromRequest(hentdokumentRequest.getTokenValidationContext())));
	}

	private TilgangVariantFormat utledTilgangHentDokument(TilgangJournalpost journalpost, Set<Ident> brukerIdenter, long dokumentInfoId, String variantFormat) {

		// Tilgang for journalpost
		var journalpostErrors = utledTilgangService.utledTilgangJournalpost(journalpost, brukerIdenter);
		if (!journalpostErrors.isEmpty()) {
			throw new HentTilgangDokumentException(journalpostErrors.getFirst().reason, lagFeilmeldingForJournalpost(journalpostErrors.getFirst()));
		}

		// Tilgang for dokument
		Optional<TilgangDokument> tilgangDokument = journalpost.getDokumenter().stream()
				.filter(dokument -> dokument.id() == dokumentInfoId)
				.findFirst();
		Optional<TilgangVariant> dokumentvariant = determineAndFindDokumentVariant(tilgangDokument, variantFormat);
		var dokumentErrors = utledTilgangService.utledTilgangDokument(journalpost, tilgangDokument.orElse(null),
						dokumentvariant.orElse(null), brukerIdenter)
				.stream()
				.filter(not(DENY_REASON_FOER_INNSYNSDATO::equals))
				.toList();
		if (!dokumentErrors.isEmpty()) {
			throw new HentTilgangDokumentException(dokumentErrors.getFirst().reason, lagFeilmeldingForDokument(dokumentErrors.getFirst()));
		}
		return dokumentvariant.map(TilgangVariant::variantformat)
				.orElseThrow(() ->
						// Merk: dette sjekkes egentlig inne i utledTilgangService.utledTilgangDokument
						new HentTilgangDokumentException(DENY_REASON_UGYLDIG_VARIANTFORMAT.reason,
								lagFeilmeldingForDokument(DENY_REASON_UGYLDIG_VARIANTFORMAT)));
	}

	private static Optional<TilgangVariant> determineAndFindDokumentVariant(Optional<TilgangDokument> tilgangDokument, String variantFormat) {
		Map<TilgangVariantFormat, TilgangVariant> dokumentvariant = tilgangDokument.stream()
				.map(TilgangDokument::dokumentvarianter)
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(TilgangVariant::variantformat, Function.identity()));
		if (variantFormat == null) {
			if (dokumentvariant.containsKey(TilgangVariantFormat.SLADDET)) {
				return Optional.of(dokumentvariant.get(TilgangVariantFormat.SLADDET));
			} else {
				return Optional.ofNullable(dokumentvariant.get(TilgangVariantFormat.ARKIV));
			}
		}
		TilgangVariantFormat tilgangVariantFormat = TilgangVariantFormat.from(variantFormat);
		if (dokumentvariant.containsKey(tilgangVariantFormat)) {
			return Optional.of(dokumentvariant.get(tilgangVariantFormat));
		}
		return Optional.empty();
	}

	private static void validerRiktigJournalpost(HentdokumentRequest hentdokumentRequest, ArkivJournalpost arkivJournalpost) {
		Long arkivJournalpostId = arkivJournalpost.journalpostId();
		if (!hentdokumentRequest.getJournalpostId().equals(arkivJournalpostId.toString())) {
			throw new IllegalStateException("Journalpost som er returnert fra dokarkiv matcher ikke journalpost fra fagarkivet. " +
					"request.journalpostId=" + hentdokumentRequest.getJournalpostId() + ", arkivJournalpost.journalpostId=" + arkivJournalpostId);
		}
	}

	private void sendHoveddokumentLestHendelse(final HentdokumentRequest hentdokumentRequest) {
		if (isNotBlank(hentdokumentRequest.getJournalpostId()) && isNotBlank(hentdokumentRequest.getDokumentInfoId())) {
			try {
				HoveddokumentLest hoveddokumentLest = new HoveddokumentLest(hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId());
				kafkaProducer.publish(hoveddokumentLest);
			} catch (Exception e) {
				log.error("Kunne ikke sende events til kafka topic={}, feilmelding={}",
						safSelvbetjeningProperties.getTopics().getDokdistdittnav(), e.getMessage(), e);
			}
		}
	}

	private Optional<Pensjonsak> hentPensjonssak(String bruker, ArkivJournalpost arkivJournalpost, Optional<Fullmakt> fullmaktOpt) {
		if (fullmaktOpt.isPresent()) {
			if (arkivJournalpost.isTilknyttetSak() && arkivJournalpost.saksrelasjon().isPensjonsak()) {
				return pensjonSakRestConsumer.hentPensjonssaker(bruker)
						.stream().filter(p -> p.sakId().equals(arkivJournalpost.saksrelasjon().sakId()))
						.findFirst();
			}
			return Optional.empty();
		}
		return Optional.empty();
	}
}
