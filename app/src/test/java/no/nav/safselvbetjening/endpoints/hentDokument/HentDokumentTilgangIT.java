package no.nav.safselvbetjening.endpoints.hentDokument;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.singletonList;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_ANNEN_PART;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_FEILREGISTRERT;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_GDPR;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_INNSYNSDATO;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_KASSERT;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_SKANNET;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_SKJULT;
import static no.nav.safselvbetjening.DenyReasonFactory.FEILMELDING_TEMAER_UNNTATT_INNSYN;
import static no.nav.safselvbetjening.NavHeaders.NAV_REASON_CODE;
import static no.nav.safselvbetjening.graphql.ErrorCode.FEILMELDING_BRUKER_KAN_IKKE_UTLEDES;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_FEILREGISTRERT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_FOER_INNSYNSDATO;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_IKKE_AVSENDER_MOTTAKER;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_KASSERT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_POL_GDPR;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_SKANNET_DOKUMENT;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_SKJULT_INNSYN;
import static no.nav.safselvbetjening.tilgang.TilgangDenyReason.DENY_REASON_TEMAER_UNNTATT_INNSYN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * Tester tilgangsregler implementasjon definert i https://confluence.adeo.no/display/BOA/safselvbetjening+-+Regler+for+innsyn
 * <p>
 * Tilgangsreglene sjekkes til en av de feiler fra topp til bunn
 */
public class HentDokumentTilgangIT extends AbstractHentDokumentItest {

	/**
	 * Tilgangregel: 1a
	 * Journalpost har bruker tilknytning i et eget bruker element (fnr) og i saksrelasjonen (aktørid)
	 * Basert på en av disse identene slår man opp dokument i PDL
	 * Hvis bruker ikke finnes i PDL så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenFeilHvisBrukerDokumentetGjelderIkkeKanUtledes() {
		stubDokarkivJournalpost();
		stubPdl("pdl-bruker-finnes-ikke.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_IKKE_AVSENDER_MOTTAKER.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_BRUKER_KAN_IKKE_UTLEDES);
	}

	/**
	 * Tilgangsregel: 1b
	 * Selvbetjening viser ikke dokumenter som er enten opprettet før eller journalført før 04.06.2016.
	 * Unntak:
	 * - tema er PEN eller UFO
	 * - innsynsflagget er satt til en VISES_-verdi
	 * @see no.nav.safselvbetjening.tilgang.UtledTilgangService Hardkodet dato
	 */
	@Test
	void skalHenteDokumentHvisInnsynVisesSelvOmEldreEnnInnsynsdato() {
		stubDokarkivJournalpost("1b-hentdokument-innsyn-vises-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	/**
	 * Tilgangsregel: 1b
	 * Selvbetjening viser ikke dokumenter som er enten opprettet før eller journalført før 04.06.2016.
	 * Unntak:
	 * - tema er PEN eller UFO
	 * - innsynsflagget er satt til en VISES_-verdi
	 * @see no.nav.safselvbetjening.tilgang.UtledTilgangService Hardkodet dato
	 */
	@Test
	void skalIkkeHenteDokumentNaarEldreEnnInnsynsdato() {
		stubDokarkivJournalpost("1b-hentdokument-eldre-enn-innsynsdato.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FOER_INNSYNSDATO.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_INNSYNSDATO);
	}

	/**
	 * Tilgangsregel: 1b
	 * Selvbetjening viser ikke dokumenter som er enten opprettet før eller journalført før 04.06.2016.
	 * Unntak:
	 * - tema er PEN eller UFO
	 * - innsynsflagget er satt til en VISES_-verdi
	 * @see no.nav.safselvbetjening.tilgang.UtledTilgangService Hardkodet dato
	 */
	@Test
	void skalHentePensjonDokumentAlderspensjonHvisEldreEnnInnsynsdato() {
		stubDokarkivJournalpost("1b-hentdokument-pensjon-eldre-enn-innsynsdato.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();
		stubPensjonssaker("hentpensjonssaker_alderspensjon_happy.json");
		stubPensjonHentBrukerForSak();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	/**
	 * Tilgangsregel: 1b
	 * Selvbetjening viser ikke dokumenter som er enten opprettet før eller journalført før 04.06.2016.
	 * Unntak:
	 * - tema er PEN eller UFO
	 * - innsynsflagget er satt til en VISES_-verdi
	 * @see no.nav.safselvbetjening.tilgang.UtledTilgangService Hardkodet dato
	 */
	@Test
	void skalHentePensjonDokumentUforetrygdHvisEldreEnnInnsynsdato() {
		stubDokarkivJournalpost("1b-hentdokument-pensjon-eldre-enn-innsynsdato.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();
		stubPensjonssaker();
		stubPensjonHentBrukerForSak();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	/**
	 * Tilgangsregel: 1c
	 * Midlertidige journalposter har status M eller MO. Det betyr at de nylig er mottatt av NAV (typisk fra skanning)
	 * Det kan være søknader bruker har under behandling og de har ingen sakstilknytning eller er ferdig journalført
	 * Hvis dokumentet er knyttet til en midlertidig journalpost og alle andre tilgangsregler er OK så skal dokumentet returneres
	 */
	@Test
	void skalHenteDokumentHvisJournalpostErMidlertidig() {
		stubDokarkivJournalpost("1c-hentdokument-midlertidig-ok.json");
		stubPdlGenerell();
		stubHentDokumentDokarkiv();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	/**
	 * Tilgangsregel: 1d
	 * Hvis dokumentet har en feilregistrert saksrelasjon så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisSaksrelasjonFeilregistrert() {
		stubDokarkivJournalpost("1d-hentdokument-feilregistrert-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_FEILREGISTRERT.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_FEILREGISTRERT);
	}

	/**
	 * Tilgangsregel: 1e
	 * Hvis dokumentet har tema unntatt innsyn på journalpost og den er midlertidig så skal det returneres Forbidden
	 *
	 * @see no.nav.safselvbetjening.domain.Tema
	 */
	@Test
	void skalGiForbiddenHvisDokumentetHarTemaSomIkkeSkalGiInnsynPaaJournalpost() {
		stubDokarkivJournalpost("1e-hentdokument-midlertidig-tema-far-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_TEMAER_UNNTATT_INNSYN.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_TEMAER_UNNTATT_INNSYN);
	}

	/**
	 * Tilgangsregel: 1e
	 * Hvis dokumentet har tema unntatt innsyn på saksrelasjon og journalposten sitt tema er feil så skal det returneres Forbidden
	 *
	 * @see no.nav.safselvbetjening.domain.Tema
	 */
	@Test
	void skalGiForbiddenHvisDokumentetHarTemaSomIkkeSkalGiInnsynPaaSaksrelasjonOgJournalpostTemaErFeil() {
		stubDokarkivJournalpost("1e-hentdokument-saksrelasjon-tema-far-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_TEMAER_UNNTATT_INNSYN.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_TEMAER_UNNTATT_INNSYN);
	}

	/**
	 * Tilgangsregel: 1f
	 * Fagpost kan skjerme dokumenter på forespørsel, det settes da et flagg på Journalpost
	 * Hvis journalpost er skjermet så skal det returneres Forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisJournalpostErSkjermet() {
		stubDokarkivJournalpost("1f-hentdokument-journalpost-skjerming-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_POL_GDPR.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_GDPR);
	}

	/**
	 * Tilgangsregel: 1h
	 * Hvis innsyn flagget er satt til en skjules verdi så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbidddenFeilHvisInnsynSkjules() {
		stubDokarkivJournalpost("1h-hentdokument-innsyn-skjules-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_SKJULT_INNSYN.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_SKJULT);
	}

	/**
	 * Tilgangsregel: 2a
	 * Hvis dokumentet ikke har innlogget bruker som avsender så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisBrukerIkkeErAvsender() {
		stubDokarkivJournalpost("2a-hentdokument-bruker-ikke-avsender-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_IKKE_AVSENDER_MOTTAKER.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_ANNEN_PART);
	}

	/**
	 * Tilgangsregel: 2b
	 * Saksbehandlere sender dokumenter fra NAV til skanning.
	 * Disse har da journalposttype utgående, journalstatus lokalprint og en skannet mottakskanal
	 * Hvis dokumentet er skannet så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisUtgaaendeJournalpostSkannet() {
		stubDokarkivJournalpost("2b-hentdokument-lokal-skannet-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_SKANNET_DOKUMENT.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_SKANNET);
	}

	/**
	 * Tilgangsregel: 2f
	 * Fagpost kan logisk slette dokumenter.
	 * Hvis dokumentet er kassert så skal det returneres en Forbidden feil
	 */
	@Test
	void skalGiForbiddenHvisDokumentErKassert() {
		stubDokarkivJournalpost("2f-hentdokument-dokument-kassert-forbidden.json");
		stubPdlGenerell();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getHeaders().get(NAV_REASON_CODE)).isEqualTo(singletonList(DENY_REASON_KASSERT.reason));
		assertThat(responseEntity.getBody()).contains(FEILMELDING_KASSERT);
	}

}
