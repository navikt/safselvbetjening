package no.nav.safselvbetjening.rest;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.dokarkiv.DokumentIkkeFunnetException;
import no.nav.safselvbetjening.consumer.dokarkiv.JournalpostIkkeFunnetException;
import no.nav.safselvbetjening.consumer.pdl.PdlFunctionalException;
import no.nav.safselvbetjening.consumer.pensjon.PensjonsakIkkeFunnetException;
import no.nav.safselvbetjening.hentdokument.HentDokument;
import no.nav.safselvbetjening.hentdokument.HentDokumentService;
import no.nav.safselvbetjening.hentdokument.HentDokumentValidator;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequest;
import no.nav.safselvbetjening.hentdokument.HentdokumentRequestException;
import no.nav.safselvbetjening.tilgang.HentTilgangDokumentException;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static no.nav.safselvbetjening.MDCUtils.MDC_CALL_ID;
import static no.nav.safselvbetjening.MDCUtils.MDC_CONSUMER_ID;
import static no.nav.safselvbetjening.MDCUtils.getConsumerIdFromToken;
import static no.nav.safselvbetjening.NavHeaders.NAV_CALLID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Endepunktet til hentDokument, som returnerer et dokument fra joark basert på journalpostId, dokumentInfoId og variantFormat.
 */
@RestController
@RequestMapping("rest/")
@Slf4j
@Protected
public class HentDokumentController {

	private final HentDokumentValidator hentDokumentValidator;
	private final HentDokumentService hentDokumentService;
	private final TokenValidationContextHolder tokenValidationContextHolder;

	public HentDokumentController(
			HentDokumentValidator hentDokumentValidator,
			HentDokumentService hentDokumentService,
			TokenValidationContextHolder tokenValidationContextHolder
	) {
		this.hentDokumentValidator = hentDokumentValidator;
		this.hentDokumentService = hentDokumentService;
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@GetMapping(value = { "hentdokument/{journalpostId}/{dokumentInfoId}", "hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}" })
	public ResponseEntity<byte[]> hentDokument(
			@PathVariable String journalpostId,
			@PathVariable String dokumentInfoId,
			@PathVariable(required = false) String variantFormat,
			@RequestHeader(value = NAV_CALLID, required = false) String navCallid) {
		try {
			final TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
			MDC.put(MDC_CALL_ID, isNotBlank(navCallid) ? navCallid : randomUUID().toString());
			MDC.put(MDC_CONSUMER_ID, getConsumerIdFromToken(tokenValidationContext));

			HentdokumentRequest request = HentdokumentRequest.builder()
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.variantFormat(variantFormat)
					.tokenValidationContext(tokenValidationContext)
					.build();
			hentDokumentValidator.validate(request);

			log.info("hentdokument har mottatt kall. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);
			HentDokument response = hentDokumentService.hentDokument(request);
			log.info("hentdokument hentet dokument. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);

			return ResponseEntity.ok()
					.contentType(response.getMediaType())
					.header(CONTENT_DISPOSITION, "inline; filename=" + dokumentInfoId + "_" + response.getVariantformat() + response.getExtension())
					.body(response.getDokument());
		} catch (HentTilgangDokumentException e) {
			String message = format("Tilgang til dokument avvist. journalpostId=%s, dokumentInfoId=%s, variantFormat=%s. reason=%s", journalpostId, dokumentInfoId, variantFormat, e.getMessage());
			log.error(message);
			throw e;
		} catch (JournalpostIkkeFunnetException | DokumentIkkeFunnetException | PdlFunctionalException |
				 PensjonsakIkkeFunnetException e) {
			log.warn(e.getMessage());
			throw new ResponseStatusException(NOT_FOUND, e.getMessage());
		} catch (HentdokumentRequestException e) {
			log.warn(e.getMessage());
			throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			MDC.clear();
		}
	}
}
