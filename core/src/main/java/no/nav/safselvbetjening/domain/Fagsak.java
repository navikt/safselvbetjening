package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Value
@Builder(toBuilder = true)
public class Fagsak {
	@Builder.Default
	@EqualsAndHashCode.Exclude
	List<Journalpost> journalposter = new ArrayList<>();
	String fagsakId;
	String fagsaksystem;
	String tema;

	public String getFagSakIdAndFagsaksystem() {
		if (isBlank(fagsakId) && isBlank(fagsaksystem)) {
			return null;
		}
		return fagsakId + "_" + fagsaksystem;
	}
}
