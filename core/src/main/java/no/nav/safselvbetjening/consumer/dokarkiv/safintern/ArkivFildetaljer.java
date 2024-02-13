package no.nav.safselvbetjening.consumer.dokarkiv.safintern;

public record ArkivFildetaljer(String skjerming,
							   String format,
							   // kun metadata, ikke brukt til tilgangskontroll
							   String stoerrelse,
							   String type,
							   String uuid
							   ) {
}
