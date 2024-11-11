package no.nav.safselvbetjening.tilgang;

/**
 * En bruker som eier en journalpost
 *
 * @param brukerId En Ident som identifiserer brukeren, enten Foedselsnummer, Organisasjonsnummer eller AktoerId
 * @see Foedselsnummer
 * @see Organisasjonsnummer
 * @see AktoerId
 */
public record TilgangBruker(Ident brukerId) {
}
