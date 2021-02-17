package no.nav.safselvbetjening.consumer.fagarkiv.domain;

import no.nav.safselvbetjening.domain.Tema;

import java.util.HashMap;
import java.util.Map;

public enum FagomradeCode {
	/**
	 * Bidrag
	 */
	BID,
	/**
	 * Pensjon
	 */
	PEN,
	/**
	 * Øvrig
	 */
	OVR,
	/**
	 * Skanning
	 */
	MOT,
	/**
	 * Okonomi
	 */
	OKO,
	/**
	 * Bidrag innkreving
	 */
	BII,
	/**
	 * FS22
	 */
	FS22,
	/**
	 * Bil
	 */
	BIL,
	/**
	 * Hjelpemidler
	 */
	HJE,
	/**
	 * Barnetrygd
	 */
	BAR,
	/**
	 * Foreldre- og svangerskapspenger
	 */
	FOR,
	/**
	 * Gravferdsstønad
	 */
	GRA,
	/**
	 * Grunn- og hjelpestønad
	 */
	GRU,
	/**
	 * Kontantstøtte
	 */
	KON,
	/**
	 * Omsorgspenger, Pleiepenger og opplæringspenger
	 */
	OMS,
	/**
	 * Supplerende stønad
	 */
	SUP,
	/**
	 * Yrkesskade / Menerstatning
	 */
	YRK,
	/**
	 * Enslig forsørger
	 */
	ENF,
	/**
	 * Stønadsregnskap
	 */
	STO,
	/**
	 * Forsikring
	 */
	FOS,
	/**
	 * Erstatning
	 */
	ERS,
	/**
	 * Saksomkostning
	 */
	SAK,
	/**
	 * Dagpenger
	 */
	DAG,
	/**
	 * Individstønad
	 */
	IND,
	/**
	 * Mob.stønad
	 */
	MOB,
	/**
	 * Oppfølging
	 */
	OPP,
	/**
	 * Ventelønn
	 */
	VEN,
	/**
	 * Yrkesrettet attføring
	 */
	YRA,
	/**
	 * Rehabilitering
	 */
	REH,
	/**
	 * Uføreytelser
	 */
	UFO,
	/**
	 * Sykepenger
	 */
	SYK,
	/**
	 * Sykemelding
	 */
	SYM,
	/**
	 * Feilutbetaling (Arenaytelser)
	 */
	FEI,
	/**
	 * Generell
	 */
	GEN,
	/**
	 * Arbeidsavklaringspenger
	 */
	AAP,
	/**
	 * Fullmakt
	 */
	FUL,
	/**
	 * Helsetjenester og ort. Hjelpemidler
	 */
	HEL,
	/**
	 * Condictio indebiti
	 */
	CON,
	/**
	 * Medlemskap
	 */
	MED,
	/**
	 * Ukjent
	 */
	UKJ,
	/**
	 * Tiltak
	 */
	TIL,
	/**
	 * Rekruttering og stilling
	 */
	REK,
	/**
	 * Inkluderende Arbeidsliv
	 */
	IAR,
	/**
	 * Ajourhold - Grunnopplysninger
	 */
	AGR,
	/**
	 * Trekk
	 */
	TRK,
	/**
	 * Kontroll
	 */
	KTR,
	/**
	 * Permittering og masseoppsigelser
	 */
	PER,
	/**
	 * AA-registeret
	 */
	AAR,
	/**
	 * Trygdeavgift
	 */
	TRY,
	/**
	 * Sanksjon - Arbeidsgiver
	 */
	SAA,
	/**
	 * Sanksjon - Person
	 */
	SAP,
	/**
	 * Oppfølging
	 */
	OPA,
	/**
	 * Serviceklager
	 */
	SER,
	/**
	 * Sikkerhetstiltak
	 */
	SIK,
	/**
	 * Unntak fra medlemskap
	 */
	UFM,
	/**
	 * Tilleggsstønad arbeidsøkere
	 */
	TSR,
	/**
	 * Tilleggsstønad
	 */
	TSO,
	/**
	 * Rettferdsvederlag
	 */
	RVE,
	/**
	 * Retting av personopplysninger
	 */
	RPO,
	/**
	 * Farskap
	 */
	FAR,
	/**
	 * Kompensasjon for selvstendig næringsdrivende/frilansere
	 */
	FRI;

	// Vennligst se https://jira.adeo.no/browse/MMA-3142
	// Tema CON har data i joark men temaet skal ikke vises.
	private static final Map<FagomradeCode, Boolean> INVALID_TEMA = new HashMap<>();

	static {
		INVALID_TEMA.put(FagomradeCode.CON, true);
	}

	public static boolean isValid(FagomradeCode joarkFagomradeCode) {
		return !INVALID_TEMA.containsKey(joarkFagomradeCode);
	}

	public static Tema toTema(FagomradeCode joarkFagomradeCode) {
		// Vennligst se https://jira.adeo.no/browse/MMA-3076 . Tema OKO korrigeres til Tema STO
		if(joarkFagomradeCode == OKO) {
			return Tema.STO;
		}
		// Hvis tema er null så faller man tilbake til UKJ
		return joarkFagomradeCode == null ? Tema.UKJ : Tema.valueOf(joarkFagomradeCode.name());
	}
}
