package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

import lombok.Builder;

import static no.nav.safselvbetjening.consumer.dokarkiv.domain.FagsystemCode.PEN;

@Builder
public record ArkivSaksrelasjon(Long sakId, String fagsystem, Boolean feilregistrert, ArkivSak sak) {
	public boolean isPensjonsak() {
		return PEN.name().equals(fagsystem);
	}
}
