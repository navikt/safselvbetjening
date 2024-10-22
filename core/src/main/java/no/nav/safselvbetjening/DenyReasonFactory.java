package no.nav.safselvbetjening;

import no.nav.safselvbetjening.tilgang.TilgangDenyReason;

public class DenyReasonFactory {

	// Feilmeldinger
	public static final String KONTAKT_OSS = " Kontakt oss på #team_dokumentløsninger hvis du har spørsmål om dette.";
	public static final String TILGANG_TIL_JOURNALPOST_AVVIST = "Tilgang til journalpost ble avvist ";
	public static final String TILGANG_TIL_DOKUMENT_AVVIST = "Tilgang til dokument ble avvist ";

	public static final String FEILMELDING_ANNEN_PART = "fordi dokumentet er sendt til/fra andre parter enn bruker.";
	public static final String FEILMELDING_INNSYNSDATO = "fordi journalposten er opprettet før tidligste innsynsdato (04.06.2016).";
	public static final String FEILMELDING_UGYLDIG_JOURNALSTATUS = "fordi journalposten ikke har status ferdigstilt eller midlertidig.";
	public static final String FEILMELDING_FEILREGISTRERT = "fordi journalposten er feilregistrert.";
	public static final String FEILMELDING_TEMAER_UNNTATT_INNSYN = "fordi journalposten er markert som kontrollsak eller farskapssak.";
	public static final String FEILMELDING_GDPR = "ihht. GDPR.";
	public static final String FEILMELDING_FORVALTNINGSNOTAT = "fordi journalposten er et notat, men hoveddokumentet er ikke et forvaltningsnotat.";
	public static final String FEILMELDING_SKJULT = "fordi journalposten er skjult.";
	public static final String FEILMELDING_SKANNET = "fordi dokumentet er skannet.";
	public static final String FEILMELDING_KASSERT = "fordi dokumentet er kassert.";
	public static final String FEILMELDING_INGEN_GYLDIG_TOKEN = "Ingen gyldige tokens i Authorization-headeren.";
	public static final String FEILMELDING_BRUKER_MATCHER_IKKE_TOKEN = "Bruker på journalpost tilhører ikke bruker i token. Innlogget bruker har heller ingen fullmakt overfor journalpost tilhørende bruker.";
	public static final String FEILMELDING_FULLMAKT_GJELDER_IKKE_FOR_TEMA = "Innlogget bruker har fullmakt overfor bruker, men fullmakten gjelder ikke for journalposten sitt tema.";
	public static final String FEILMELDING_UGYLDIG_VARIANT = "Variantformat må være enten SLADDET eller ARKIV.";


	public static String lagFeilmeldingForJournalpost(TilgangDenyReason grunn) {
		return TILGANG_TIL_JOURNALPOST_AVVIST + medGrunn(grunn) + KONTAKT_OSS;
	}

	public static String lagFeilmeldingForDokument(TilgangDenyReason grunn) {
		return TILGANG_TIL_DOKUMENT_AVVIST + medGrunn(grunn) + KONTAKT_OSS;
	}

	public static String medGrunn(TilgangDenyReason reason) {
		return switch (reason) {
			case DENY_REASON_ANNEN_PART -> FEILMELDING_ANNEN_PART;
			case DENY_REASON_INNSYNSDATO -> FEILMELDING_INNSYNSDATO;
			case DENY_REASON_UGYLDIG_JOURNALSTATUS -> FEILMELDING_UGYLDIG_JOURNALSTATUS;
			case DENY_REASON_FEILREGISTRERT -> FEILMELDING_FEILREGISTRERT;
			case DENY_REASON_TEMAER_UNNTATT_INNSYN -> FEILMELDING_TEMAER_UNNTATT_INNSYN;
			case DENY_REASON_GDPR -> FEILMELDING_GDPR;
			case DENY_REASON_FORVALTNINGSNOTAT -> FEILMELDING_FORVALTNINGSNOTAT;
			case DENY_REASON_SKJULT_INNSYN -> FEILMELDING_SKJULT;
			case DENY_REASON_SKANNET_DOKUMENT -> FEILMELDING_SKANNET;
			case DENY_REASON_UGYLDIG_VARIANTFORMAT -> FEILMELDING_UGYLDIG_VARIANT;
			case DENY_REASON_KASSERT -> FEILMELDING_KASSERT;
		};
	}
}
