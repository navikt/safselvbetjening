package no.nav.safselvbetjening.hentdokument;

import no.nav.safselvbetjening.consumer.fagarkiv.FagarkivConsumer;
import no.nav.safselvbetjening.consumer.fagarkiv.HentDokumentResponseTo;
import no.nav.safselvbetjening.consumer.fagarkiv.tilgangjournalpost.TilgangJournalpostResponseTo;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentDokumentService {
	private final FagarkivConsumer fagarkivConsumer;

	public HentDokumentService(FagarkivConsumer fagarkivConsumer) {
		this.fagarkivConsumer = fagarkivConsumer;
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
		// FIXME dokument tilgangskontroll
	}
}
