package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.domain.JournalStatusCode;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostDto;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import no.nav.safselvbetjening.consumer.pensjon.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.UtledTilgangHentDokumentService;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.FS22;
import static no.nav.safselvbetjening.consumer.fagarkiv.domain.FagsystemCode.PEN;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentDokumentService {
	private final FagarkivConsumer fagarkivConsumer;
	private final IdentService identService;
	private final UtledTilgangHentDokumentService utledTilgangHentDokumentService;
	private final PensjonSakRestConsumer pensjonSakRestConsumer;

	public HentDokumentService(FagarkivConsumer fagarkivConsumer, IdentService identService,
							   UtledTilgangHentDokumentService utledTilgangHentDokumentService,
							   PensjonSakRestConsumer pensjonSakRestConsumer) {
		this.fagarkivConsumer = fagarkivConsumer;
		this.identService = identService;
		this.utledTilgangHentDokumentService = utledTilgangHentDokumentService;
		this.pensjonSakRestConsumer = pensjonSakRestConsumer;
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
		final BrukerIdenter brukerIdenter = identService.hentIdenter(bruker);

		utledTilgangHentDokumentService.utledTilgangHentDokumen(tilgangJournalpostResponseTo.getTilgangJournalpostDto(), brukerIdenter);
	}

	private String findBrukerIdent(TilgangJournalpostDto tilgangJournalpostDto) {
		if (JournalStatusCode.getJournalstatusMidlertidig().contains(tilgangJournalpostDto.getJournalStatus())) {
			return tilgangJournalpostDto.getBruker().getBrukerId();
		} else if (JournalStatusCode.getJournalstatusFerdigstilt().contains(tilgangJournalpostDto.getJournalStatus())) {
			if (FS22.toString().equals(tilgangJournalpostDto.getSak().getApplikasjon())) {
				return tilgangJournalpostDto.getSak().getAktoerId();
			} else if (PEN.toString().equals(tilgangJournalpostDto.getSak().getApplikasjon())) {
				return pensjonSakRestConsumer.hentBrukerForSak(tilgangJournalpostDto.getSak().getSakId()).getFnr();
			}
		}
		return null;
	}
}
