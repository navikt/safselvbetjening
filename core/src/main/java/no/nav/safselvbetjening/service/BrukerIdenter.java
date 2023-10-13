package no.nav.safselvbetjening.service;

import lombok.Getter;
import no.nav.safselvbetjening.consumer.pdl.PdlResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrukerIdenter {

	@Getter
	private String aktivAktoerId;

	@Getter
	private String aktivFolkeregisterident;

	private final List<String> aktoerIds = new ArrayList<>();
	private final List<String> foedselsnummer = new ArrayList<>();

	public BrukerIdenter(final List<PdlResponse.PdlIdent> pdlIdenter) {
		for (PdlResponse.PdlIdent pdlIdent : pdlIdenter) {
			switch (pdlIdent.getGruppe()) {
				case AKTORID -> {
					if (!pdlIdent.isHistorisk()) {
						this.aktivAktoerId = pdlIdent.getIdent();
					}
					this.aktoerIds.add(pdlIdent.getIdent());
				}
				case FOLKEREGISTERIDENT -> {
					if (!pdlIdent.isHistorisk()) {
						this.aktivFolkeregisterident = pdlIdent.getIdent();
					}
					this.foedselsnummer.add(pdlIdent.getIdent());
				}
				default -> {
					// noop
				}
			}
		}
	}

	public List<String> getAktoerIds() {
		return Collections.unmodifiableList(aktoerIds);
	}

	public List<String> getFoedselsnummer() {
		return Collections.unmodifiableList(foedselsnummer);
	}

	public List<String> getIdenter() {
		return Stream.concat(getAktoerIds().stream(), getFoedselsnummer().stream()).collect(Collectors.toList());
	}

	public boolean isEmpty() {
		return aktoerIds.isEmpty() && foedselsnummer.isEmpty();
	}

	public static BrukerIdenter empty() {
		return new BrukerIdenter(List.of());
	}

	/**
	 * Ikke risikere å skrive ut identer ved kall til toString()
	 *
	 * @return Oversikt over hva dette objektet inneholder men ikke hvilke aktørId/fnr
	 */
	@Override
	public String toString() {
		return "BrukerIdenter{antallAktoerIds=" + aktoerIds.size() + ", antallFoedselsnummer=" + foedselsnummer.size() + "}";
	}
}
