package no.nav.safselvbetjening.tilgang;

public enum TilgangDenyReason {
	DENY_REASON_ANNEN_PART("annen_part"),
	DENY_REASON_INNSYNSDATO("opprettet_for_innsynsdato"),
	DENY_REASON_UGYLDIG_JOURNALSTATUS("ugyldig_journalstatus"),
	DENY_REASON_FEILREGISTRERT("feilregistrert"),
	DENY_REASON_TEMAER_UNNTATT_INNSYN("temaer_unntatt_innsyn"),
	DENY_REASON_GDPR("gdpr"),
	DENY_REASON_FORVALTNINGSNOTAT("forvaltningsnotat"),
	DENY_REASON_SKJULT_INNSYN("skjult_innsyn"),
	DENY_REASON_SKANNET_DOKUMENT("skannet_dokument"),
	DENY_REASON_UGYLDIG_VARIANTFORMAT("deny_reason_ugyldig_variantformat"),
	DENY_REASON_KASSERT("kassert_dokument");

	public final String reason;

	TilgangDenyReason(String reason) {
		this.reason = reason;
	}
}
