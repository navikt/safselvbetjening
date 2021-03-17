package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.safselvbetjening.service.IdentService;
import no.nav.safselvbetjening.tilgang.UtledTilgangDokumentService;
import no.nav.safselvbetjening.tilgang.UtledTilgangJournalpostService;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentDokumentService {
	private final FagarkivConsumer fagarkivConsumer;
	private final IdentService identService;
	private final UtledTilgangJournalpostService utledTilgangJournalpostService;
	private final UtledTilgangDokumentService utledTilgangDokumentService;

	public HentDokumentService(FagarkivConsumer fagarkivConsumer, IdentService identService,
							   UtledTilgangJournalpostService utledTilgangJournalpostService,
							   UtledTilgangDokumentService utledTilgangDokumentService) {
		this.fagarkivConsumer = fagarkivConsumer;
		this.identService = identService;
		this.utledTilgangJournalpostService = utledTilgangJournalpostService;
		this.utledTilgangDokumentService = utledTilgangDokumentService;
	}

	public HentDokument hentDokument(final String journalpostId, final String dokumentInfoId, final String variantFormat, final String ident) {
		final BrukerIdenter brukerIdenter = identService.hentIdenter(ident);

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


		//utledTilgangJournalpostService.utledTilgangJournalpost(tilgangJournalpostResponseTo.getTilgangJournalpostDto())

	}
}
