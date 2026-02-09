package no.nav.safselvbetjening.domain;

import no.nav.safselvbetjening.tilgang.UtledTilgangService;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.tilgang.UtledTilgangService.GJELDENDE_TEMA_UNNTATT_INNSYN;

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
	OLJ("Oljepionerene"),
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
	STO("Regnskap/utbetaling/årsoppgave"),
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
	AKT("Aktivitetsplan med dialoger"),
	UNG("Ungdomsprogramytelsen"),
	PAI("Innsyn"),
	POI("Innsyn etter personopplysningsloven"),
	// Listen under skal ikke vises til brukere
	FAR("Foreldreskap"),
	KTR("Kontroll"),
	KTA("Kontroll – anmeldelse"),
	ARS("Arbeidsrådgivning – skjermet"),
	ARP("Arbeidsrådgivning – psykologtester"),
	BBF("Barnebortføring");

	private final String temanavn;

	Tema(final String temanavn) {
		this.temanavn = temanavn;
	}

	public String getTemanavn() {
		return temanavn;
	}

	private static final EnumSet<Tema> TEMA_UNNTATT_INNSYN = EnumSet.copyOf(GJELDENDE_TEMA_UNNTATT_INNSYN.stream()
			.map(Tema::valueOf)
			.toList());

	/// Tema som ikke skal vises til bruker
	///
	/// @return EnumSet med tema som ikke skal vises til bruker
	/// @see UtledTilgangService
	public static EnumSet<Tema> unntattInnsynNavNo() {
		return TEMA_UNNTATT_INNSYN;
	}

	public static Set<String> unntattInnsynNavNoString() {
		return unntattInnsynNavNo().stream().map(Tema::name).collect(Collectors.toSet());
	}

	public static EnumSet<Tema> tillattInnsynNavNo() {
		return EnumSet.complementOf(unntattInnsynNavNo());
	}

	public static List<String> tillattInnsynNavNoString() {
		return tillattInnsynNavNo().stream().map(Tema::name).collect(Collectors.toList());
	}
}
