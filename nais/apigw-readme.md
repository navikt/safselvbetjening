# APIGW config for safselvbetjening

Se også https://confluence.adeo.no/display/TDOK/HOWTO+-+Api-gw

Endepunkt med tjenester:
* `https://api-gw-q1.adeo.no/safselvbetjening/graphql` (graphql tjenesten)
* `https://api-gw-q1.adeo.no/safselvbetjening/rest_hentdokument` (rest_hentdokument tjenesten)

`PUT https://api-management.prod-fss.nais.io/rest/v2/katalog/applikasjoner/safselvbetjening`
Headere:

```
kilde: noFasit
Authorization: Basic base64(brukernavn:passord)
```

Body

```json
{
  "eier": "Group_8528f622-a170-42e4-9d63-6640e1d1a21d",
  "sone": "FraFss",
  "tjenester": [
    {
      "navn": "graphql",
      "endepunkt": "https://safselvbetjening.dev.intern.nav.no/graphql",
      "miljo": "q1"
    },
    {
      "navn": "rest_hentdokument",
      "endepunkt": "https://safselvbetjening.dev.intern.nav.no/rest/hentdokument",
      "miljo": "q1"
    },
    {
      "navn": "graphql",
      "endepunkt": "https://safselvbetjening.intern.nav.no/graphql",
      "miljo": "p"
    },
    {
      "navn": "rest_hentdokument",
      "endepunkt": "https://safselvbetjening.intern.nav.no/rest/hentdokument",
      "miljo": "p"
    }
  ],
  "konsumenter": [
    {
      "navn": "mininnboks-api-q0",
      "tjeneste": "graphql"
    },
    {
      "navn": "mininnboks-api-q0",
      "tjeneste": "rest_hentdokument"
    },
    {
      "navn": "mininnboks-api-q1",
      "tjeneste": "graphql"
    },
    {
      "navn": "mininnboks-api-q1",
      "tjeneste": "rest_hentdokument"
    },
    {
      "navn": "mininnboks-api",
      "tjeneste": "graphql"
    },
    {
      "navn": "mininnboks-api",
      "tjeneste": "rest_hentdokument"
    }
  ]
}
```
