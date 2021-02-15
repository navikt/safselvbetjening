package no.nav.safselvbetjening.consumer.sak;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ArkivsakConsumer {
    private static final String HEADER_SAK_CORRELATION_ID = "X-Correlation-ID";

    private final RestTemplate restTemplate;
    private final String sakUrl;

    public ArkivsakConsumer(final RestTemplateBuilder restTemplateBuilder,
                            final SafSelvbetjeningProperties safSelvbetjeningProperties) {
        this.sakUrl = safSelvbetjeningProperties.getEndpoints().getSak();
        this.restTemplate = restTemplateBuilder
                .setReadTimeout(Duration.ofSeconds(20))
                .setConnectTimeout(Duration.ofSeconds(5))
                .basicAuthentication(safSelvbetjeningProperties.getServiceuser().getUsername(),
                        safSelvbetjeningProperties.getServiceuser().getPassword())
                .build();
    }

    public List<Arkivsak> hentSaker(final List<String> aktoerId, final List<String> tema) {
        final UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(sakUrl)
                .queryParam("aktoerId", aktoerId)
                .queryParam("tema", tema);
        return hentSaker(uri.toUriString());
    }

    private List<Arkivsak> hentSaker(final String uri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HEADER_SAK_CORRELATION_ID, getOrGenerateCorrelationId());
            ResponseEntity<List<Arkivsak>> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
            });
            return response.getBody();
        } catch (HttpServerErrorException e) {
            throw new ConsumerTechnicalException("Teknisk feil. Kunne ikke hente saker for bruker fra sak.", e);
        } catch (HttpClientErrorException e) {
            throw new ConsumerFunctionalException("Funksjonell feil. Kunne ikke hente saker for bruker fra sak.", e);
        }
    }

    private String getOrGenerateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
