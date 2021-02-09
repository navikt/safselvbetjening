package no.nav.safselvbetjening.consumer.pdl;

import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.azure.AzureTokenConsumer;
import no.nav.safselvbetjening.consumer.azure.TokenResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * PDL implementasjon av {@link IdentConsumer}
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class PdlIdentConsumer implements IdentConsumer {
    private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
    private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

    private final RestTemplate restTemplate;
    private final URI pdlUri;
    private final AzureTokenConsumer azureTokenConsumer;

    public PdlIdentConsumer(final SafSelvbetjeningProperties safSelvbetjeningProperties,
                            final RestTemplateBuilder restTemplateBuilder,
                            final AzureTokenConsumer azureTokenConsumer) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
        this.pdlUri = UriComponentsBuilder.fromHttpUrl(safSelvbetjeningProperties.getEndpoints().getPdl()).build().toUri();
        this.azureTokenConsumer = azureTokenConsumer;
    }

    @Retryable(
            include = HttpServerErrorException.class
    )
    @Override
    public List<String> hentAktoerIder(final String folkeregisterIdent) throws PersonIkkeFunnetException {
        try {
            final RequestEntity<PdlRequest> requestEntity = baseRequest()
                    .body(mapHentAktoerIdForFolkeregisterident(folkeregisterIdent));
            final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

            if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
                return pdlResponse.getData().getHentIdenter().getIdenter().stream().map(PdlResponse.PdlIdent::getIdent).collect(Collectors.toList());
            } else {
                if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
                    throw new PersonIkkeFunnetException("Fant ikke aktørid for person i pdl.");
                }
                throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
            }
        } catch (HttpClientErrorException e) {
            throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
        }
    }

    private PdlRequest mapHentAktoerIdForFolkeregisterident(final String ident) {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("ident", ident);
        return PdlRequest.builder()
                .query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: AKTORID, historikk: true) {identer { ident gruppe historisk } } }")
                .variables(variables)
                .build();
    }

    @Retryable(
            include = HttpServerErrorException.class
    )
    @Override
    public List<String> hentFolkeregisterIdenter(final String aktoerId) throws PersonIkkeFunnetException {
        try {
            final RequestEntity<PdlRequest> requestEntity = baseRequest()
                    .body(mapHentFolkeregisterIdentForAktoerId(aktoerId));
            final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

            if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
                return pdlResponse.getData().getHentIdenter().getIdenter().stream().map(PdlResponse.PdlIdent::getIdent).collect(Collectors.toList());
            } else {
                if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
                    throw new PersonIkkeFunnetException("Fant ikke folkeregisterident for person i pdl.");
                }
                throw new PdlFunctionalException("Kunne ikke hente folkeregisterident for aktørid i pdl. " + pdlResponse.getErrors());
            }
        } catch (HttpClientErrorException e) {
            throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
        }
    }

    private PdlRequest mapHentFolkeregisterIdentForAktoerId(final String ident) {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("ident", ident);
        return PdlRequest.builder()
                .query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: FOLKEREGISTERIDENT, historikk: true) {identer { ident gruppe historisk } } }")
                .variables(variables)
                .build();
    }

    private RequestEntity.BodyBuilder baseRequest() {
        TokenResponse clientCredentialToken = azureTokenConsumer.getClientCredentialToken();
        return RequestEntity.post(pdlUri)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientCredentialToken.getAccess_token())
                .header(HEADER_PDL_NAV_CONSUMER_TOKEN, "Bearer " + clientCredentialToken.getAccess_token());
    }
}