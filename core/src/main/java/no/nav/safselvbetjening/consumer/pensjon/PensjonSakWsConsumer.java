package no.nav.safselvbetjening.consumer.pensjon;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.ConsumerFunctionalException;
import no.nav.safselvbetjening.consumer.ConsumerTechnicalException;
import no.nav.safselvbetjening.consumer.PersonIkkeFunnetException;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeRequest;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeResponse;
import org.springframework.stereotype.Component;

import javax.xml.ws.soap.SOAPFaultException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class PensjonSakWsConsumer {
    private static final String PENSJON_INSTANCE = "pensjon";

    private final PensjonSakV1 pensjonSakV1;
    private static final int MILLI_TO_NANO_CONST = 1000000;

    public PensjonSakWsConsumer(PensjonSakV1 pensjonSakV1) {
        this.pensjonSakV1 = pensjonSakV1;
    }

    @Retry(name = PENSJON_INSTANCE)
    @CircuitBreaker(name = PENSJON_INSTANCE)
    public List<Pensjonsak> hentSakSammendragListe(final String personident) {
        WSHentSakSammendragListeRequest request = new WSHentSakSammendragListeRequest();
        request.setPersonident(personident);

        try {
            WSHentSakSammendragListeResponse response = pensjonSakV1.hentSakSammendragListe(request);
            return response.getSakSammendragListe().stream().map(saksammendrag ->
                    Pensjonsak.builder()
                            .sakNr(saksammendrag.getSakId())
                            .tema(saksammendrag.getArkivtema().getValue())
                            .datoOpprettet(saksammendrag.getSaksperiode().getFom() == null ? null :
                                    jodaToJavaLocalDateTime(saksammendrag.getSaksperiode().getFom().toDateTimeAtStartOfDay().toLocalDateTime()))
                            .build())
                    .collect(Collectors.toList());
        } catch (HentSakSammendragListeSakManglerEierenhet e) {
            throw new ConsumerFunctionalException("Funksjonell feil mot PensjonSak_v1.hentSakSammendragListe. Personen ble funnet, men en av sakene mangler eierenhet.", e);
        } catch (HentSakSammendragListePersonIkkeFunnet e) {
            throw new ConsumerFunctionalException("Funksjonell feil mot PensjonSak_v1.hentSakSammendragListe. Personen ble ikke funnet.", e);
        } catch (SOAPFaultException e) {
            // Se https://jira.adeo.no/browse/TEST-40974 for grunnen til at dette er her
            // Workaround for å komme rundt at pensjon ikke oppfyller kontraktene sine
            if (e.getMessage().contains("cvc-particle 3.1: in element {http://nav.no/tjeneste/virksomhet/pensjonSak/v1}hentSakSammendragListepersonIkkeFunnet of type {http://nav.no/tjeneste/virksomhet/pensjonSak/v1/feil}PersonIkkeFunnet, found </a:hentSakSammendragListepersonIkkeFunnet> (in namespace http://nav.no/tjeneste/virksomhet/pensjonSak/v1), but next item should be feilkilde")) {
                throw new PersonIkkeFunnetException("Personen ble ikke funnet i psak.", e);
            } else {
                throw new ConsumerTechnicalException("Teknisk feil mot PensjonSak_v1.hentSakSammendragListe", e);
            }
        } catch (Exception e) {
            throw new ConsumerTechnicalException("Teknisk feil mot PensjonSak_v1.hentSakSammendragListe", e);
        }
    }

    private static LocalDateTime jodaToJavaLocalDateTime(org.joda.time.LocalDateTime localDateTime) {
        return LocalDateTime.of(
                localDateTime.getYear(),
                localDateTime.getMonthOfYear(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHourOfDay(),
                localDateTime.getMinuteOfHour(),
                localDateTime.getSecondOfMinute(),
                localDateTime.getMillisOfSecond() * MILLI_TO_NANO_CONST);
    }
}
