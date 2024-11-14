package no.nav.safselvbetjening.tilgang;

/**
 * En bruker som eier en journalpost
 *
 * @param brukerId En Ident som identifiserer brukeren, enten Foedselsnummer, Organisasjonsnummer eller AktoerId
 */
public record TilgangBruker(Ident brukerId) {
}
