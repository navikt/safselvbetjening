package no.nav.safselvbetjening.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Dokumentoversikt {

	private final String code;

	@Builder.Default
	private final List<String> temaliste;

	@Builder.Default
	private final List<String> temanavnListe;

	@Builder.Default
	private final List<Journalpost> journalposter = new ArrayList<>();

	public static Dokumentoversikt empty() {
		return new Dokumentoversikt(null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
}
