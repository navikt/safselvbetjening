package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import no.nav.safselvbetjening.consumer.pdl.PdlFunctionalException;
import no.nav.safselvbetjening.consumer.pensjon.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.safselvbetjening.tilgang.UtledTilgangService;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;
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

	public HentDokumentService(FagarkivConsumer fagarkivConsumer, IdentService identService,
							   UtledTilgangService utledTilgangService, PensjonSakRestConsumer pensjonSakRestConsumer,
							   HentDokumentTilgangMapper hentDokumentTilgangMapper) {
		this.fagarkivConsumer = fagarkivConsumer;
		this.identService = identService;
		this.utledTilgangService = utledTilgangService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
		this.hentDokumentTilgangMapper = hentDokumentTilgangMapper;
	}

	public HentDokument hentDokument(final String journalpostId, final String dokumentInfoId, final String variantFormat) {
		doTilgangskontroll(journalpostId, dokumentInfoId, variantFormat);

		final HentDokumentResponseTo hentDokumentResponseTo = fagarkivConsumer.hentDokument(dokumentInfoId, variantFormat);
		return HentDokument.builder()
				.dokument(Base64.getDecoder().decode(hentDokumentResponseTo.getDokument()))
				.mediaType(hentDokumentResponseTo.getMediaType())
				.extension(MimetypeFileextensionMapper.toFileextension(hentDokumentResponseTo.getMediaType()))
				.build();
	}

	private void doTilgangskontroll(String journalpostId, String dokumentInfoId, String variantFormat) {
		final TilgangJournalpostResponseTo tilgangJournalpostResponseTo = fagarkivConsumer.tilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);

		final String bruker = findBrukerIdent(tilgangJournalpostResponseTo.getTilgangJournalpostDto());
		if (isBlank(bruker)) {
			throw new HentTilgangDokumentException(PARTSINNSYN, "Tilgang til dokument avvist fordi bruker ikke kan utledes");
		}
		final BrukerIdenter brukerIdenter = identService.hentIdenter(bruker);
		if (brukerIdenter.isEmpty()) {
			throw new PdlFunctionalException("Finner ingen identer på person i pdl.");
		}

		utledTilgangService.utledTilgangHentDokument(hentDokumentTilgangMapper.map(tilgangJournalpostResponseTo.getTilgangJournalpostDto()), brukerIdenter);
	}

	private String findBrukerIdent(TilgangJournalpostDto tilgangJournalpostDto) {
		if (JournalStatusCode.getJournalstatusMidlertidig().contains(tilgangJournalpostDto.getJournalStatus())) {
			return tilgangJournalpostDto.getBruker().getBrukerId();
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(tilgangJournalpostDto.getJournalStatus())) {
			if (PEN.toString().equals(tilgangJournalpostDto.getSak().getFagsystem())) {
				return pensjonSakRestConsumer.hentBrukerForSak(tilgangJournalpostDto.getSak().getSakId()).getFnr();
			} else {
				return tilgangJournalpostDto.getSak().getAktoerId();
			}
		}
		return null;
	}
}
