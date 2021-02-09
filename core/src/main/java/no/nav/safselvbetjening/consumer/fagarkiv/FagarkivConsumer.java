package no.nav.safselvbetjening.consumer.fagarkiv;

import no.nav.safselvbetjening.NavHeaders;
import no.nav.safselvbetjening.SafSelvbetjeningProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class FagarkivConsumer {
    private final RestTemplate restTemplate;

    @Autowired
    public FagarkivConsumer(final RestTemplateBuilder restTemplateBuilder,
                            final SafSelvbetjeningProperties safSelvbetjeningProperties) {
        restTemplate = restTemplateBuilder
                .rootUri(safSelvbetjeningProperties.getEndpoints().getFagarkiv())
                .basicAuthentication(safSelvbetjeningProperties.getServiceuser().getUsername(),
                        safSelvbetjeningProperties.getServiceuser().getPassword())
                .setReadTimeout(Duration.ofSeconds(60))
                .setConnectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public FinnJournalposterResponseTo finnJournalposter(FinnJournalposterRequestTo request) {
        ResponseEntity<FinnJournalposterResponseTo> response = callFinnJournalposter(request);
        return response.getBody();
    }

    private ResponseEntity<FinnJournalposterResponseTo> callFinnJournalposter(FinnJournalposterRequestTo requestTo) {
        HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(requestTo, createCorrelationIdHeader());
        return restTemplate.exchange("/finnjournalposter", HttpMethod.POST, requestEntity, FinnJournalposterResponseTo.class);
    }

    private HttpHeaders createCorrelationIdHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(NavHeaders.NAV_CALLID, UUID.randomUUID().toString());
        return headers;
    }
}
