package no.nav.safselvbetjening.hentdokument;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static no.nav.safselvbetjening.domain.Variantformat.ARKIV;
import static no.nav.safselvbetjening.domain.Variantformat.SLADDET;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentDokumentValidator {
	private static final Set<String> ALLOWED_VARIANTFORMAT = new HashSet<>(asList(ARKIV.name(), SLADDET.name()));
	private static final String VARIANTFORMAT_ERRORMSG = String.join(",", ALLOWED_VARIANTFORMAT);

	void validate(HentdokumentRequest hentdokumentRequest) {
		validateJournalpostId(hentdokumentRequest.getJournalpostId());
		validateDokumentInfoId(hentdokumentRequest.getDokumentInfoId());
		validateVariantFormat(hentdokumentRequest.getVariantFormat());
	}

	private void validateJournalpostId(String journalpostId) {
		if (!isNumeric(journalpostId)) {
			throw new HentdokumentRequestException("journalpostId er ikke et tall. journalpostId=" + journalpostId);
		}
	}

	private void validateDokumentInfoId(String dokumentInfoId) {
		if (!isNumeric(dokumentInfoId)) {
			throw new HentdokumentRequestException("dokumentInfoId er ikke et tall. dokumentInfoId=" + dokumentInfoId);
		}
	}

	private void validateVariantFormat(String variantFormat) {
		if (!ALLOWED_VARIANTFORMAT.contains(variantFormat)) {
			throw new HentdokumentRequestException("variantFormat må være en av [" + VARIANTFORMAT_ERRORMSG + "]. variantFormat=" + variantFormat);
		}
	}
}
