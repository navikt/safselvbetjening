package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import no.nav.safselvbetjening.consumer.pdl.PdlFunctionalException;
import no.nav.safselvbetjening.consumer.pensjon.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.safselvbetjening.domain.Journalpost;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.BRUKER_MATCHER_IKKE_TOKEN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.INGEN_GYLDIG_TOKEN;
import static no.nav.safselvbetjening.tilgang.DokumentTilgangMessage.PARTSINNSYN;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentDokumentService {
	private final FagarkivConsumer fagarkivConsumer;
	private final IdentService identService;
	private final UtledTilgangService utledTilgangService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;
	private final HentDokumentTilgangMapper hentDokumentTilgangMapper;
	private final HentDokumentValidator hentDokumentValidator;

	public HentDokumentService(FagarkivConsumer fagarkivConsumer, IdentService identService,
							   UtledTilgangService utledTilgangService, PensjonSakRestConsumer pensjonSakRestConsumer,
							   HentDokumentTilgangMapper hentDokumentTilgangMapper, HentDokumentValidator hentDokumentValidator) {
		this.fagarkivConsumer = fagarkivConsumer;
		this.identService = identService;
		this.utledTilgangService = utledTilgangService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.hentDokumentTilgangMapper = hentDokumentTilgangMapper;
		this.hentDokumentValidator = hentDokumentValidator;
	}

	public HentDokument hentDokument(final HentdokumentRequest hentdokumentRequest) {
		hentDokumentValidator.validate(hentdokumentRequest);
		doTilgangskontroll(hentdokumentRequest);

		final HentDokumentResponseTo hentDokumentResponseTo =
				fagarkivConsumer.hentDokument(hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat());
		return HentDokument.builder()
				.dokument(Base64.getDecoder().decode(hentDokumentResponseTo.getDokument()))
				.mediaType(hentDokumentResponseTo.getMediaType())
				.extension(MimetypeFileextensionMapper.toFileextension(hentDokumentResponseTo.getMediaType()))
				.build();
	}

	private void doTilgangskontroll(final HentdokumentRequest hentdokumentRequest) {
		final TilgangJournalpostResponseTo tilgangJournalpostResponseTo =
				fagarkivConsumer.tilgangJournalpost(hentdokumentRequest.getJournalpostId(),
						hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat());

		final String bruker = findBrukerIdent(tilgangJournalpostResponseTo.getTilgangJournalpostDto());
		if (isBlank(bruker)) {
			throw new HentTilgangDokumentException(PARTSINNSYN, "Tilgang til dokument avvist fordi bruker ikke kan utledes");
		}
		final BrukerIdenter brukerIdenter = identService.hentIdenter(bruker);
		if (brukerIdenter.isEmpty()) {
			throw new PdlFunctionalException("Finner ingen identer på person i pdl.");
		}

		validateTokenIdent(brukerIdenter, hentdokumentRequest);

		Journalpost journalpost = hentDokumentTilgangMapper.map(tilgangJournalpostResponseTo.getTilgangJournalpostDto(), brukerIdenter);
		utledTilgangService.utledTilgangHentDokument(journalpost, brukerIdenter);
	}

	private String findBrukerIdent(TilgangJournalpostDto tilgangJournalpostDto) {
		if (JournalStatusCode.getJournalstatusMidlertidig().contains(tilgangJournalpostDto.getJournalStatus())) {
			if(tilgangJournalpostDto.getBruker() == null) {
				return null;
			} else {
				return tilgangJournalpostDto.getBruker().getBrukerId();
			}
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(tilgangJournalpostDto.getJournalStatus())) {
			if (PEN.toString().equals(tilgangJournalpostDto.getSak().getFagsystem())) {
				return pensjonSakRestConsumer.hentBrukerForSak(tilgangJournalpostDto.getSak().getSakId()).getFnr();
			} else {
				return tilgangJournalpostDto.getSak().getAktoerId();
			}
		}
		return null;
	}

	private void validateTokenIdent(BrukerIdenter brukerIdenter, HentdokumentRequest hentdokumentRequest) {
		JwtToken jwtToken = hentdokumentRequest.getTokenValidationContext().getFirstValidToken()
				.orElseThrow(() -> new HentTilgangDokumentException(INGEN_GYLDIG_TOKEN, "Ingen gyldige tokens i Authorization headeren."));
		List<String> identer = brukerIdenter.getIdenter();
		String pid = jwtToken.getJwtTokenClaims().getStringClaim("pid");
		if(!identer.contains(pid)) {
			throw new HentTilgangDokumentException(BRUKER_MATCHER_IKKE_TOKEN, "Bruker på journalpost tilhører ikke bruker i token.");
		}
	}
}
