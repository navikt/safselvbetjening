package no.nav.safselvbetjening.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import no.nav.safselvbetjening.consumer.pdl.PdlFunctionalException;
import no.nav.safselvbetjening.consumer.pensjon.PensjonSakRestConsumer;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.schemas.HoveddokumentLest;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.E;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode.FS;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalpostTypeCode.U;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_INGEN_GYLDIG_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.DENY_REASON_PARTSINNSYN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DenyReasonFactory.FEILMELDING_INGEN_GYLDIG_TOKEN;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class HentDokumentService {
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	private static final EnumSet<JournalStatusCode> KAFKA_FERDIGSTILT = EnumSet.of(FS, E);

	private final FagarkivConsumer fagarkivConsumer;
	private final IdentService identService;
	private final UtledTilgangService utledTilgangService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final HentDokumentTilgangMapper hentDokumentTilgangMapper;
	private final HentDokumentValidator hentDokumentValidator;
	private final KafkaEventProducer kafkaProducer;
	private final SafSelvbetjeningProperties safSelvbetjeningProperties;


	public HentDokumentService(
			FagarkivConsumer fagarkivConsumer,
			IdentService identService,
			UtledTilgangService utledTilgangService,
			PensjonSakRestConsumer pensjonSakRestConsumer,
			HentDokumentTilgangMapper hentDokumentTilgangMapper,
			HentDokumentValidator hentDokumentValidator,
			KafkaEventProducer kafkaProducer,
			SafSelvbetjeningProperties safSelvbetjeningProperties
	) {
		this.fagarkivConsumer = fagarkivConsumer;
		this.identService = identService;
		this.utledTilgangService = utledTilgangService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.hentDokumentTilgangMapper = hentDokumentTilgangMapper;
		this.hentDokumentValidator = hentDokumentValidator;
		this.kafkaProducer = kafkaProducer;
		this.safSelvbetjeningProperties = safSelvbetjeningProperties;
	}

	public HentDokument hentDokument(final HentdokumentRequest hentdokumentRequest) {
		hentDokumentValidator.validate(hentdokumentRequest);
		TilgangJournalpostResponseTo tilgangJournalpostResponseTo = this.doTilgangskontroll(hentdokumentRequest);

		final HentDokumentResponseTo hentDokumentResponseTo = fagarkivConsumer.hentDokument(
				hentdokumentRequest.getDokumentInfoId(),
				hentdokumentRequest.getVariantFormat()
		);

		//Journalpost må ha type 'U' (utgaaende) og JournalStatus enten 'FS' (ferdigstilt) eller 'E' (ekspedert) for at vi skal sende kafkamelding
		if (U.equals(tilgangJournalpostResponseTo.getTilgangJournalpostDto().getJournalpostType()) &&
			KAFKA_FERDIGSTILT.contains(tilgangJournalpostResponseTo.getTilgangJournalpostDto().getJournalStatus())) {
			this.doSendKafkaMelding(hentdokumentRequest);
		}

		return HentDokument.builder()
				.dokument(hentDokumentResponseTo.getDokument())
				.mediaType(hentDokumentResponseTo.getMediaType())
				.extension(MimetypeFileextensionMapper.toFileextension(hentDokumentResponseTo.getMediaType()))
				.build();
	}

	private TilgangJournalpostResponseTo doTilgangskontroll(final HentdokumentRequest hentdokumentRequest) {
		final TilgangJournalpostResponseTo tilgangJournalpostResponseTo =
				fagarkivConsumer.tilgangJournalpost(
						hentdokumentRequest.getJournalpostId(),
						hentdokumentRequest.getDokumentInfoId(),
						hentdokumentRequest.getVariantFormat()
				);

		final String bruker = findBrukerIdent(tilgangJournalpostResponseTo.getTilgangJournalpostDto());
		if (isBlank(bruker)) {
			throw new HentTilgangDokumentException(DENY_REASON_PARTSINNSYN, FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
		}

		final BrukerIdenter brukerIdenter = identService.hentIdenter(bruker);
		if (brukerIdenter.isEmpty()) {
			throw new PdlFunctionalException("Finner ingen identer på person i pdl.");
		}

		validateTokenIdent(brukerIdenter, hentdokumentRequest);

		Journalpost journalpost = hentDokumentTilgangMapper.map(tilgangJournalpostResponseTo.getTilgangJournalpostDto(), brukerIdenter);
		utledTilgangService.utledTilgangHentDokument(journalpost, brukerIdenter);

		return tilgangJournalpostResponseTo;
	}

	private void doSendKafkaMelding(final HentdokumentRequest hentdokumentRequest) {
		if (isNotBlank(hentdokumentRequest.getJournalpostId()) && isNotBlank(hentdokumentRequest.getDokumentInfoId())) {
			try {
				HoveddokumentLest hoveddokumentLest = new HoveddokumentLest(hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId());
				kafkaProducer.publish(hoveddokumentLest);
			} catch (Exception e) {
				log.error("Kunne ikke sende events til kafka topic={}, feilmelding={}", safSelvbetjeningProperties.getTopics().getDokdistdittnav(), e.getMessage(), e);
			}
		}

	}

	private String findBrukerIdent(TilgangJournalpostDto tilgangJournalpostDto) {
		if (JournalStatusCode.getJournalstatusMidlertidig().contains(tilgangJournalpostDto.getJournalStatus())) {
			if (tilgangJournalpostDto.getBruker() == null) {
				return null;
			} else {
				return tilgangJournalpostDto.getBruker().getBrukerId();
			}
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(tilgangJournalpostDto.getJournalStatus())) {
			if (PEN.toString().equals(tilgangJournalpostDto.getSak().getFagsystem())) {
				return pensjonSakRestConsumer.hentBrukerForSak(tilgangJournalpostDto.getSak().getSakId()).fnr();
			} else {
				return tilgangJournalpostDto.getSak().getAktoerId();
			}
		}
		return null;
	}

	private void validateTokenIdent(
			BrukerIdenter brukerIdenter,
			HentdokumentRequest hentdokumentRequest
	) {
		JwtToken jwtToken = hentdokumentRequest.getTokenValidationContext().getFirstValidToken()
				.orElseThrow(() -> new HentTilgangDokumentException(DENY_REASON_INGEN_GYLDIG_TOKEN, FEILMELDING_INGEN_GYLDIG_TOKEN));
		List<String> identer = brukerIdenter.getIdenter();
		String pid = jwtToken.getJwtTokenClaims().getStringClaim(CLAIM_PID);
		String sub = jwtToken.getJwtTokenClaims().getStringClaim(CLAIM_SUB);
		if (!identer.contains(pid) && !identer.contains(sub)) {
			secureLog.warn("hentdokument(journalpostId={}, dokumentInfoId={}, variantFormat={}) Innlogget bruker med ident={} matcher ikke bruker på journalpost. brukerIdenter={}",
					hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat(),
					pidOrSub(pid, sub), identer);
			throw new HentTilgangDokumentException(DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN, FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN);
		}
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
