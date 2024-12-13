package no.nav.safselvbetjening.tilgang;

/**
 * TilgangDenyReason inneholder alle feilkodene som kan komme om en bruker skal nektes tilgang til en Journalpost eller
 * et dokument. Kodene kan med fordel sendes ut i en eventuell respons til konsumenten, men bør ledsages av en menneske-vennlig tekst.
 */
public enum TilgangDenyReason {
	DENY_REASON_IKKE_AVSENDER_MOTTAKER("ikke_avsender_mottaker"),
	DENY_REASON_FOER_INNSYNSDATO("foer_innsynsdato"),
	DENY_REASON_UGYLDIG_JOURNALSTATUS("ugyldig_journalstatus"),
	DENY_REASON_FEILREGISTRERT("feilregistrert"),
	DENY_REASON_TEMAER_UNNTATT_INNSYN("temaer_unntatt_innsyn"),
	DENY_REASON_POL_GDPR("pol_gdpr"),
	DENY_REASON_NOTAT("notat"),
	DENY_REASON_SKJULT_INNSYN("skjult_innsyn"),
	DENY_REASON_SKANNET_DOKUMENT("skannet_dokument"),
	DENY_REASON_UGYLDIG_VARIANTFORMAT("ugyldig_variantformat"),
	DENY_REASON_KASSERT("kassert_dokument");

	public final String reason;

	TilgangDenyReason(String reason) {
		this.reason = reason;
	}
}
