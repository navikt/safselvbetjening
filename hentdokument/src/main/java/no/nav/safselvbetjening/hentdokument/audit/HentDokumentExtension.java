package no.nav.safselvbetjening.hentdokument.audit;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;
import no.nav.safselvbetjening.hentdokument.audit.cef.Extension;

import static java.lang.String.format;

@Slf4j
@SuperBuilder
public class HentDokumentExtension extends Extension {
	final HentdokumentRequest hentdokumentRequest;

	@Override
	protected String getDeviceCustomStringsCef() {
		return format("flexString1=%s flexString1Label=journalpostId flexString2=%s flexString2Label=dokumentInfoId cs3=%s cs3Label=variantFormat",
				hentdokumentRequest.getJournalpostId(), hentdokumentRequest.getDokumentInfoId(), hentdokumentRequest.getVariantFormat());
	}
}
