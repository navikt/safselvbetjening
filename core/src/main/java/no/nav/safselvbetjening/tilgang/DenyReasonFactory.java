package no.nav.safselvbetjening.tilgang;

public class DenyReasonFactory {

	public static final String DENY_REASON_PARTSINNSYN = "ingen_partsinnsyn";
	public static final String DENY_REASON_INNSYNSDATO = "opprettet_for_innsynsdato";
	public static final String DENY_REASON_UGYLDIG_JOURNALSTATUS = "ugyldig_journalstatus";
	public static final String DENY_REASON_FEILREGISTRERT = "feilregistrert";
	public static final String DENY_REASON_TEMAER_UNNTATT_INNSYN = "temaer_unntatt_innsyn";
	public static final String DENY_REASON_GDPR = "gdpr";
	public static final String DENY_REASON_FORVALTNINGSNOTAT = "forvaltningsnotat";
	public static final String DENY_REASON_ORGANINTERNT = "organinternt";
	public static final String DENY_REASON_SKJULT_INNSYN = "skjult_innsyn";
	public static final String DENY_REASON_ANNEN_PART = "annen_part";
	public static final String DENY_REASON_SKANNET_DOKUMENT = "skannet_dokument";
	public static final String DENY_REASON_INNSKRENKET_PARTSINNSYN = "innskrenket_partsinnsyn";
	public static final String DENY_REASON_KASSERT = "kassert_dokument";
	public static final String DENY_REASON_INGEN_GYLDIG_TOKEN = "ingen_gyldig_token";
	public static final String DENY_REASON_BRUKER_MATCHER_IKKE_TOKEN = "bruker_matcher_ikke_token";

	// Feilmeldinger
	public static final String KONTAKT_OSS = " Kontakt oss på #team_dokumentløsninger hvis du har spørsmål om dette.";
	public static final String TILGANG_TIL_JOURNALPOST_AVVIST = "Tilgang til journalpost ble avvist ";
	public static final String TILGANG_TIL_DOKUMENT_AVVIST = "Tilgang til dokument ble avvist ";

	public static final String FEILMELDING_PARTSINNSYN = "fordi bruker ikke er part i journalposten.";
	public static final String FEILMELDING_INNSYNSDATO = "fordi journalposten er opprettet før tidligste innsynsdato (04.06.2016).";
	public static final String FEILMELDING_UGYLDIG_JOURNALSTATUS = "fordi journalposten ikke har status ferdigstilt eller midlertidig.";
	public static final String FEILMELDING_FEILREGISTRERT = "fordi journalposten er feilregistrert.";
	public static final String FEILMELDING_TEMAER_UNNTATT_INNSYN = "fordi journalposten er markert som kontrollsak eller farskapssak.";
	public static final String FEILMELDING_GDPR = "ihht. GDPR.";
	public static final String FEILMELDING_FORVALTNINGSNOTAT = "fordi journalposten er et notat, men hoveddokumentet er ikke et forvaltningsnotat.";
	public static final String FEILMELDING_ORGANINTERNT = "fordi ett eller flere dokumenter på journalposten er markert som organinternt.";
	public static final String FEILMELDING_SKJULT = "fordi journalposten er skjult.";
	public static final String FEILMELDING_ANNEN_PART = "fordi dokumentet er sendt til/fra andre parter enn bruker.";
	public static final String FEILMELDING_SKANNET = "fordi dokumentet er skannet.";
	public static final String FEILMELDING_INNSKRENKET_PARTSINNSYN = "fordi dokumentet er markert med innskrenket partsinnsyn.";
	public static final String FEILMELDING_KASSERT = "fordi dokumentet er kassert.";
	public static final String FEILMELDING_INGEN_GYLDIG_TOKEN = "Ingen gyldige tokens i Authorization-headeren.";
	public static final String FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN = "Bruker på journalpost tilhører ikke bruker i token. Ident må ligge i pid eller sub claim.";

	public static String lagFeilmeldingForJournalpost(String grunn) {
		return TILGANG_TIL_JOURNALPOST_AVVIST + grunn + KONTAKT_OSS;
	}

	public static String lagFeilmeldingForDokument(String grunn) {
		return TILGANG_TIL_DOKUMENT_AVVIST + grunn + KONTAKT_OSS;
	}

}
