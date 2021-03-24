package no.nav.safselvbetjening.rest;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.hentdokument.HentDokument;
import no.nav.safselvbetjening.hentdokument.HentDokumentService;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;

/**
 * Endepunktet til hentDokument, som returnerer et dokument fra joark basert på journalpostId, dokumentInfoId og variantFormat.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@RestController
@RequestMapping("rest/")
@Slf4j
@Protected
public class HentDokumentController {
	private final HentDokumentService hentDokumentService;

	public HentDokumentController(HentDokumentService hentDokumentService) {
		this.hentDokumentService = hentDokumentService;
	}

	@GetMapping(value = "hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}")
	public ResponseEntity<byte[]> hentDokument(
			@PathVariable String journalpostId,
			@PathVariable String dokumentInfoId,
			@PathVariable String variantFormat,
			@RequestHeader(value = NAV_CALLID, required = false) String navCallid) {
		log.info("hentdokument har mottatt kall. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);
		try {
			HentDokument response = hentDokumentService.hentDokument(journalpostId, dokumentInfoId, variantFormat);
			log.info("hentDokument hentet dokument. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);

			return ResponseEntity.ok()
					.contentType(response.getMediaType())
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + dokumentInfoId + "_" + variantFormat + response.getExtension())
					.body(response.getDokument());
		} catch (HentTilgangDokumentException e) {
			log.warn("Tilgang til dokument avvist. journalpostId={}, dokumentInfoId={}, variantFormat={}. Feilmelding={}", journalpostId, dokumentInfoId, variantFormat, e.getMessage());
			throw e;
		}
	}
}
