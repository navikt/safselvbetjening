package no.nav.safselvbetjening.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import static no.nav.safselvbetjening.domain.Sakstype.FAGSAK;

@Value
@Builder
public class Sak {
	String fagsakId;
	String fagsaksystem;
	Sakstype sakstype;

	@JsonIgnore
	public boolean isFagsak() {
		return FAGSAK == sakstype;
	}
}
