package no.nav.safselvbetjening.domain;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Tema {
	AAP("Arbeidsavklaringspenger"),
	AAR("Aa-registeret"),
	AGR("Ajourhold – grunnopplysninger"),
	BAR("Barnetrygd"),
	BID("Bidrag"),
	BIL("Bil"),
	DAG("Dagpenger"),
	ENF("Enslig mor eller far"),
	ERS("Erstatning"),
	FEI("Feilutbetaling"),
	FOR("Foreldre- og svangerskapspenger"),
	FOS("Forsikring"),
	FRI("Kompensasjon for selvstendig næringsdrivende/frilansere"),
	FUL("Fullmakt"),
	GEN("Generell"),
	GRA("Gravferdsstønad"),
	GRU("Grunn- og hjelpestønad"),
	HEL("Helsetjenester og ortopediske hjelpemidler"),
	HJE("Hjelpemidler"),
	IAR("Inkluderende arbeidsliv"),
	IND("Tiltakspenger"),
	KON("Kontantstøtte"),
	MED("Medlemskap"),
	MOB("Mobilitetsfremmende stønad"),
	OMS("Omsorgspenger, pleiepenger og opplæringspenger"),
	OPA("Oppfølging – arbeidsgiver"),
	OPP("Oppfølging"),
	PEN("Pensjon"),
	PER("Permittering og masseoppsigelser"),
	REH("Rehabiliteringspenger"),
	REK("Rekruttering"),
	RPO("Retting av personopplysninger"),
	RVE("Rettferdsvederlag"),
	SAA("Sanksjon - Arbeidsgiver"),
	SAK("Sakskostnader"),
	SAP("Sanksjon – person"),
	SER("Serviceklager"),
	SIK("Sikkerhetstiltak"),
	STO("Regnskap/utbetaling"),
	SUP("Supplerende stønad"),
	SYK("Sykepenger"),
	SYM("Sykmeldinger"),
	TIL("Tiltak"),
	TRK("Trekkhåndtering"),
	TRY("Trygdeavgift"),
	TSO("Tilleggsstønad"),
	TSR("Tilleggsstønad – arbeidssøkere"),
	UFM("Unntak fra medlemskap"),
	UFO("Uføretrygd"),
	UKJ("Ukjent"),
	VEN("Ventelønn"),
	YRA("Yrkesrettet attføring"),
	YRK("Yrkesskade og menerstatning"),
	FIP("Fiskerpensjon"),
	KLL("Klage – lønnsgaranti"),
	EYB("Barnepensjon"),
	EYO("Omstillingsstønad"),
	// Listen under skal ikke vises til brukere
	FAR("Foreldreskap"),
	KTR("Kontroll"),
	KTA("Kontroll – anmeldelse"),
	ARS("Arbeidsrådgivning – skjermet"),
	ARP("Arbeidsrådgivning – psykologtester");

	private final String temanavn;

	Tema(final String temanavn) {
		this.temanavn = temanavn;
	}

	public String getTemanavn() {
		return temanavn;
	}

	/**
	 * Tema som ikke skal vises til bruker
	 *
	 * @return Set med tema som ikke skal vises til bruker
	 */
	public static EnumSet<Tema> brukerHarIkkeInnsyn() {
		return EnumSet.of(FAR, KTR, KTA, ARS, ARP);
	}

	public static Set<String> brukerHarIkkeInnsynAsString() {
		return brukerHarIkkeInnsyn().stream().map(Tema::name).collect(Collectors.toSet());
	}

	public static EnumSet<Tema> brukerHarInnsyn() {
		return EnumSet.complementOf(brukerHarIkkeInnsyn());
	}

	public static List<String> brukerHarInnsynAsListString() {
		return brukerHarInnsyn().stream().map(Tema::name).collect(Collectors.toList());
	}
}
