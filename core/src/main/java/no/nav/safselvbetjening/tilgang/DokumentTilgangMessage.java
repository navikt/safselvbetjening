package no.nav.safselvbetjening.tilgang;

public class DokumentTilgangMessage {

	public DokumentTilgangMessage() {
	}

	public static final String STATUS_OK = "ok";
	public static final String PARTSINNSYN = "Dokumentet er sendt til/fra andre parter enn brukeren.";
	public static final String SKANNET_DOKUMENT = "Brukeren får ikke se skannede dokumenter";
	public static final String INNSKRENKET_PARTSINNSYN = "Dokumenter markert som innskrenketPartsinnsyn skal ikke vises";
	public static final String GDPR = "Dokumenter som er begrenset ihht. gdpr";
	public static final String KASSERT = "Kasserte dokumenter skal ikke vises";
}
