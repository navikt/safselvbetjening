safselvbetjening
================

## Funksjonalitet

safselvbetjening tilbyr tjenester for innsyn til dokumentene til bruker.

Tjenesten støtter ikke oppslag fra saksbehandlere. For dette skal man bruke [saf](https://github.com/navikt/saf).

For mer informasjon: [safselvbetjening systemdokumentasjon](https://confluence.adeo.no/display/BOA/safselvbetjening)

## API

#### Endepunkt

| cluster    | fss-til-fss     | gcp-til-fss                       | sbs-til-fss        |
| :--------- | :-------------- | :-------------------------------- | :----------------- |
| `dev-fss`  | `https://safselvbetjening.dev.intern.nav.no`  |`https://safselvbetjening.dev-fss-pub.nais.io`| `https://api-gw-q1.adeo.no/safselvbetjening` |
| `prod-fss` | `https://safselvbetjening.intern.nav.no` |`https://safselvbetjening.prod-fss-pub.nais.io`|  `https://api-gw.adeo.no/safselvbetjening` |

Kontakt teamet for å bli lagt til som api-gw konsument.

#### Autorisasjon

APIene krever [maskin-til-maskin token på vegne av innbygger](https://security.labs.nais.io/pages/guide/api-kall/sluttbruker/idporten.html).

Kun tokenx vekslede tokens er støttet. Brukers fødselsnummer må ligge i `pid` eller `sub` claimet.

For tilgang, ta kontakt med teamet eller oppdater `ACCESS_POLICY_INBOUND_RULES` i [q2-config.json](nais/q2-config.json)/[p-config.json](nais/p-config.json) i en PR og informer teamet.

#### GraphQL dokumentoversiktSelvbetjening

| Header        | Type     | Beskrivelse                                |
| :--------------- | :------- | :----------------------------------------- |
| `Authorization`  | `string` | **Påkrevd**. Autorisasjon til tjenesten. `Bearer <token>` |
| `Nav-Callid`  | `string` | **Valgfri**. Sporing for på tvers av verdikjeder. Helst en GUID eller annen unik ID. |
```http
  POST /graphql
```

For oppbygging av query, se spec. Bruk en GraphQL klient som f.eks [Altair](https://altair.sirmuel.design/) for å gjøre introspeksjon. Typer og felt skal ha dokumentasjon. 

Public graphql spec (tilgjengelig fra internett) på `gh-pages` branchen: `https://navikt.github.io/safselvbetjening/schema.graphqls`

##### Suksess

HttpStatus: `200 OK`

Eksempel query:
```
{
  dokumentoversiktSelvbetjening(ident: "11111111111", tema: []) {
    tema {
      kode
      navn
    }
  }
}
```

Eksempel respons:

```
{
  "data": {
    "dokumentoversiktSelvbetjening": {
      "tema": [
        {
          "kode": "AAP",
          "navn": "Arbeidsavklaringspenger"
        },
        {
          "kode": "BAR",
          "navn": "Barnetrygd"
        }
      ]
    }
  }
}
```

##### Feil

HttpStatus: `200 OK`

Feilmeldinger propageres i `errors` i respons.

Anbefales å logge `errors[0..n].message`. Samt klassifisering i `errors[0..n].extensions.code`.

| `extensions.code`  | Beskrivelse                                |
| :----------------- | :----------------------------------------- |
| `bad_request`      | Feil i input til queries. |
| `unauthorized`     | Ingen tilgang til tjenesten. Ugyldig token. Token som ikke tilhører ident i query. |
| `not_found`        | Bruker finnes ikke i PDL. Ingen saker på bruker. |
| `server_error`     | Intern teknisk feil som ikke er håndtert. |

Eksempel:

```
{
  "errors": [
    {
      "message": "Ident argumentet er ugyldig. Det må være et fødselsnummer eller en aktørid.",
      "locations": [
        {
          "line": 2,
          "column": 3
        }
      ],
      "path": [
        "dokumentoversiktSelvbetjening"
      ],
      "extensions": {
        "code": "bad_request",
        "classification": "ValidationError"
      }
    }
  ],
  "data": null
}
```

#### Hent dokument

| Header        | Type     | Beskrivelse                                |
| :--------------- | :------- | :----------------------------------------- |
| `Authorization`  | `string` | **Påkrevd**. Autorisasjon til tjenesten. `Bearer <token>` |
| `Nav-Callid`  | `string` | **Valgfri**. Sporing for på tvers av verdikjeder. Helst en GUID eller annen unik ID. |
```http
  GET /rest/hentdokument/${journalpostId}/${dokumentInfoId}/${variantFormat}
```
**api-gw** variant:
```http
  GET /rest_hentdokument/${journalpostId}/${dokumentInfoId}/${variantFormat}
```

| Parameter        | Type     | Beskrivelse                                |
| :--------------- | :------- | :----------------------------------------- |
| `journalpostId`  | `string` | **Påkrevd**. JournalpostId til dokumentet. |
| `dokumentInfoId` | `string` | **Påkrevd**. DokumentInfoId til dokumentet. |
| `variantFormat`  | `string` | **Påkrevd**. VariantFormat. Gyldige verdier: `ARKIV`. |

##### Suksess

Returnerer `200 OK`

| Header                 | Eksempel | Beskrivelse                                |
| :--------------------- | :------- | :----------------------------------------- |
| `Content-Disposition`  | `inline; filename=40000000_ARKIV.pdf` | Avhengig av hvordan konsument ønsker vise filen. Filnavn hvis den lastes ned. |
| `Content-Type`         | `application/pdf` | Mimetype for filen. Konsument velger hvordan den vises. |

Returnerer en base64 encodet representasjon av dokumentet i payload.

##### Feil

| Header               | Eksempel            | Beskrivelse                                                             |
|:---------------------|:--------------------|:------------------------------------------------------------------------|
| `Nav-Reason-Code`    | `ingen_partsinnsyn` | Grunnen til at dokumentet ikke hentes for visning. Se liste lenger ned. |

Returnerer:
* `400 Bad Request` - Dokumentet eller metadata tilhørende dokumentet finnes ikke. Bruker finnes ikke.
* `401 Unauthorized` - Dokumentet tilhører ikke bruker i token. Ingen tilgang til dokumentet basert på [regler](https://confluence.adeo.no/pages/viewpage.action?pageId=377182021).
* `404 Not Found` - Dokumentet eller metadata tilhørende dokumentet finnes ikke. Bruker finnes ikke.

| Nav-Reason-Code           | Beskrivelse                                                                                                                                             |
|:--------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------|
| ingen_partsinnsyn         | Bruker må være part for å se journalposter                                                                                                              |
| opprettet_for_innsynsdato | Bruker får ikke se journalposter som er opprettet før 04.06.2016                                                                                        |
| skannet_dokument          | Bruker får ikke se skannede dokumenter                                                                                                                  |
| innskrenket_partsinnsyn   | Dokumenter markert som innskrenketPartsinnsyn skal ikke vises                                                                                           |
| gdpr                      | Dokumenter som er begrenset ihht. gdpr                                                                                                                  |
| kassert_dokument          | Kasserte dokumenter skal ikke vises                                                                                                                     |
| ugyldig_journalstatus     | Bruker får kun se midlertidige og ferdigstilte journalposter                                                                                            |
| feilregistrert            | Bruker får ikke se feilregistrerte journalposter                                                                                                        |
| temaer_unntatt_innsyn     | Bruker får ikke innsyn i temaer unntatt innsyn (kontrollsaker og farskapssaker)                                                                         |
| forvaltningsnotat         | Bruker får ikke innsyn i notater med mindre det er et forvaltningsnotat                                                                                 |
| organinternt              | Bruker får ikke innsyn i journalposter der ett eller flere dokumenter er markert som organinternt                                                       |
| annen_part                | Dokumenter som er sendt til/fra andre parter enn bruker, skal ikke vises                                                                                |
| bruker_matcher_ikke_token | Bruker på dokumentet matcher ikke bruker i token                                                                                                        |
| skjult_innsyn             | Innsynsreglene styrer utvalget av journalposter og dokumenter som en innlogget bruker får innsyn i på nav.no. Bruker får ikke se skjulte journalposter. |


## Utvikling
### Forutsetninger
* JDK 17
* Maven 3

### Bygging

For å kjøre tester:
```
mvn clean verify
```

For å lage `app.jar`:
```
mvn clean package
```
Finn kjørbart artifact i `app/target/app.jar`

### Starte appen lokalt
Start appen lokalt i IntelliJ:
Profile: `nais` (prod-likt) eller `local` (bedre logging)
Angi VM-options fra vault:  https://vault.adeo.no/ui/vault/secrets/secret/show/dokument/safselvbetjening

Det kan hende clientSecret fra Azure er utløpt. [Da må dette hentes på nytt](https://confluence.adeo.no/display/TDOK/HOWTO+-+Azure). Snakk med teamet hvis det behøves.

## Deploy
Deploy av appen blir gjort vha. Github Actions.

## Drift

* [Logger i kibana](https://logs.adeo.no/goto/a5a525568150732ceea57fd572172dc5). Basert på kibana søk: `application:safselvbetjening AND cluster:prod-fss`.

## Support
Support for tjenester på denne appen kan rettes til Team Dokumentløsninger på slack:
* [\#team_dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)
