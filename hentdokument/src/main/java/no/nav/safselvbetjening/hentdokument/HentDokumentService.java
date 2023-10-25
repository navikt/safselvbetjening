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
import no.nav.safselvbetjening.fullmektig.FullmektigService;
import no.nav.safselvbetjening.hentdokument.audit.HentDokumentAudit;
import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.safselvbetjening.CoreConfig.SYSTEM_CLOCK;
import static no.nav.safselvbetjening.MDCUtils.MDC_FULLMAKT_TEMA;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_INGEN_GYLDIG_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_INGEN_GYLDIG_TOKEN;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class HentDokumentService {
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	public static final Set<String> HENTDOKUMENT_TILGANG_FIELDS = Set.of(
			"journalpostId", "fagomraade", "status", "type", "skjerming", "mottakskanal", "utsendingskanal", "innsyn",
			"bruker", "avsenderMottaker", "relevanteDatoer", "saksrelasjon",
			"dokumenter.dokumentInfoId", "dokumenter.tilknyttetSom", "dokumenter.kassert", "dokumenter.kategori", "dokumenter.skjerming", "dokumenter.fildetaljer");

	private final DokarkivConsumer dokarkivConsumer;
	private final IdentService identService;
	private final FullmektigService fullmektigService;
	private final UtledTilgangService utledTilgangService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final HentDokumentTilgangMapper hentDokumentTilgangMapper;
	private final KafkaEventProducer kafkaProducer;
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;
	private final HentDokumentAudit audit;

	public HentDokumentService(
			DokarkivConsumer dokarkivConsumer,
			IdentService identService,
			FullmektigService fullmektigService,
			UtledTilgangService utledTilgangService,
			PensjonSakRestConsumer pensjonSakRestConsumer,
			HentDokumentTilgangMapper hentDokumentTilgangMapper,
			KafkaEventProducer kafkaProducer,
			SafSelvbetjeningProperties safSelvbetjeningProperties
	) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.identService = identService;
		this.fullmektigService = fullmektigService;
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
				hentdokumentRequest.getVariantFormat()
		);

		if (tilgangskontroll.genererHoveddokumentLestHendelse()) {
			sendHoveddokumentLestHendelse(hentdokumentRequest);
		}

		return HentDokument.builder()
				.dokument(hentDokumentResponseTo.getDokument())
				.mediaType(hentDokumentResponseTo.getMediaType())
				.extension(MimetypeFileextensionMapper.toFileextension(hentDokumentResponseTo.getMediaType()))
				.build();
	}

	private Tilgangskontroll doTilgangskontroll(final HentdokumentRequest hentdokumentRequest) {
		ArkivJournalpost arkivJournalpost = dokarkivConsumer.journalpost(hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), HENTDOKUMENT_TILGANG_FIELDS);
		validerRiktigJournalpost(hentdokumentRequest, arkivJournalpost);
		final BrukerIdenter brukerIdenter = identService.hentIdenter(arkivJournalpost);
		if (brukerIdenter.isEmpty()) {
			throw new HentTilgangDokumentException(DENY_REASON_PARTSINNSYN, FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
		}

		Optional<Fullmakt> fullmaktOpt = validerInnloggetBrukerOgFinnFullmakt(brukerIdenter, hentdokumentRequest);
		Optional<Pensjonsak> pensjonsakOpt = hentPensjonssak(brukerIdenter.getAktivFolkeregisterident(), arkivJournalpost, fullmaktOpt);
		Journalpost journalpost = hentDokumentTilgangMapper.map(arkivJournalpost, hentdokumentRequest.getVariantFormat(), brukerIdenter, pensjonsakOpt);
		validerFullmakt(hentdokumentRequest, fullmaktOpt, journalpost);

		utledTilgangService.utledTilgangHentDokument(journalpost, brukerIdenter);
		recordFullmaktAuditLog(fullmaktOpt, hentdokumentRequest);

		return new Tilgangskontroll(journalpost, fullmaktOpt);
	}

	private static void validerRiktigJournalpost(HentdokumentRequest hentdokumentRequest, ArkivJournalpost arkivJournalpost) {
		Long arkivJournalpostId = arkivJournalpost.journalpostId();
		if (!hentdokumentRequest.getJournalpostId().equals(arkivJournalpostId.toString())) {
			throw new IllegalStateException("Journalpost som er returnert fra dokarkiv matcher ikke journalpost fra fagarkivet. " +
											"request.journalpostId=" + hentdokumentRequest.getJournalpostId() + ", arkivJournalpost.journalpostId=" + arkivJournalpostId);
		}
	}

	private static void validerFullmakt(HentdokumentRequest hentdokumentRequest, Optional<Fullmakt> fullmaktOpt, Journalpost journalpost) {
		if (fullmaktOpt.isPresent()) {
			Fullmakt fullmakt = fullmaktOpt.get();
			String gjeldendeTema = journalpost.getTilgang().getGjeldendeTema();
			if (fullmakt.gjelderForTema(gjeldendeTema)) {
				secureLog.info("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}, tema={}) Innlogget bruker med ident={} bruker fullmakt med tema={} for dokument tilhørende bruker={}",
						hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(), gjeldendeTema,
						fullmakt.fullmektig(), fullmakt.tema(), fullmakt.fullmaktsgiver());
			} else {
				secureLog.warn("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}, tema={}) Innlogget bruker med ident={} har fullmakt som ikke dekker tema for dokument tilhørende bruker={}. Tilgang er avvist",
						hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(), gjeldendeTema,
						fullmakt.fullmektig(), fullmakt.fullmaktsgiver());
				throw new HentTilgangDokumentException(DENY_REASON_FULLMAKT_GJELDER_IKKE_FOR_TEMA, FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA);
			}
		}
	}

	private void recordFullmaktAuditLog(Optional<Fullmakt> fullmaktOpt, HentdokumentRequest hentdokumentRequest) {
		fullmaktOpt.ifPresent(fullmakt -> audit.logSomFullmektig(fullmakt, hentdokumentRequest));
	}

	private void sendHoveddokumentLestHendelse(final HentdokumentRequest hentdokumentRequest) {
		if (isNotBlank(hentdokumentRequest.getJournalpostId()) && isNotBlank(hentdokumentRequest.getDokumentInfoId())) {
			try {
				HoveddokumentLest hoveddokumentLest = new HoveddokumentLest(hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId());
				kafkaProducer.publish(hoveddokumentLest);
			} catch (Exception e) {
				log.error("Kunne ikke sende events til kafka topic={}, feilmelding={}", safSelvbetjeningProperties.getTopics().getDokdistdittnav(), e.getMessage(), e);
			}
		}
	}

	private Optional<Fullmakt> validerInnloggetBrukerOgFinnFullmakt(
			BrukerIdenter brukerIdenter,
			HentdokumentRequest hentdokumentRequest
	) {
		JwtToken subjectJwt = hentdokumentRequest.getTokenValidationContext().getFirstValidToken()
				.orElseThrow(() -> new HentTilgangDokumentException(DENY_REASON_INGEN_GYLDIG_TOKEN, FEILMELDING_INGEN_GYLDIG_TOKEN));
		List<String> identer = brukerIdenter.getIdenter();
		String pid = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_PID);
		String sub = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_SUB);
		if (!identer.contains(pid) && !identer.contains(sub)) {
			Optional<Fullmakt> fullmakt = fullmektigService.finnFullmakt(subjectJwt, brukerIdenter.getAktivFolkeregisterident());
			if (fullmakt.isPresent()) {
				MDC.put(MDC_FULLMAKT_TEMA, fullmakt.get().tema().toString());
				return fullmakt;
			} else {
				secureLog.warn("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}) Innlogget bruker med ident={} matcher ikke bruker på journalpost og har ingen fullmakt. brukerIdenter={}",
						hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(),
						pidOrSub(pid, sub), identer);
				throw new HentTilgangDokumentException(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
			}
		}
		return Optional.empty();
	}

	private Optional<Pensjonsak> hentPensjonssak(String bruker, ArkivJournalpost arkivJournalpost, Optional<Fullmakt> fullmaktOpt) {
		if (fullmaktOpt.isPresent()) {
			if (arkivJournalpost.isTilknyttetSak() && arkivJournalpost.saksrelasjon().isPensjonsak()) {
				return pensjonSakRestConsumer.hentPensjonssaker(bruker)
						.stream().filter(p -> p.sakId().equals(arkivJournalpost.saksrelasjon().sakId().toString()))
						.findFirst();
			}
			return Optional.empty();
		}
		return Optional.empty();
	}

	private String pidOrSub(String pid, String sub) {
		if (isNotBlank(pid)) {
			return pid;
		}
		if (isNotBlank(sub)) {
			return sub;
		}
		return null;
	}
}
