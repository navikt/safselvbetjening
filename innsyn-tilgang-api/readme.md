innsyn-tilgang-api
==================

`innsyn-tilgang-api` er et bibliotek som inneholder logikken som avgjør om
en bruker får tilgang til å se dokumenter som er sendt til eller fra dem i
`safselvbetjening`.

Logikken er skilt ut til et eget bibliotek for å gjøre det
mulig å gjøre oppslag på om en bruker har tilgang til et gitt dokument f.eks. i
`saf`, slik at vi kan eksponere den informasjonen til saksbehandlere i Nav.

For et eksempel på en implementasjon som bruker biblioteket, kan du se i
`safselvbetjening`.
