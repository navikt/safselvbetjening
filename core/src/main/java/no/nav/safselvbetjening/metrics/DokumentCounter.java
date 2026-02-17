package no.nav.safselvbetjening.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class DokumentCounter {
	private final Counter bucket0_30;
	private final Counter bucket31_180;
	private final Counter bucket181_365;
	private final Counter bucket366_730;
	private final Counter bucket731_1825;
	private final Counter bucket1826plus;

	@Autowired
	public DokumentCounter(MeterRegistry meterRegistry) {
		bucket0_30      = meterRegistry.counter("dok_alder_bucket", "range", "0_30");
		bucket31_180    = meterRegistry.counter("dok_alder_bucket", "range", "31_180");
		bucket181_365   = meterRegistry.counter("dok_alder_bucket", "range", "181_365");
		bucket366_730   = meterRegistry.counter("dok_alder_bucket", "range", "366_730");
		bucket731_1825  = meterRegistry.counter("dok_alder_bucket", "range", "731_1825");
		bucket1826plus  = meterRegistry.counter("dok_alder_bucket", "range", "1826_plus");

	}

	public void loggAlderDokumentMetrikk(OffsetDateTime datoOpprettet){
		long dagerGammel = ChronoUnit.DAYS.between(datoOpprettet, OffsetDateTime.now());

		if (dagerGammel <= 30) bucket0_30.increment();
		else if (dagerGammel <= 180) bucket31_180.increment();
		else if (dagerGammel <= 365) bucket181_365.increment();
		else if (dagerGammel <= 730) bucket366_730.increment();
		else if (dagerGammel <= 1825) bucket731_1825.increment();
		else bucket1826plus.increment();

		log.info("Logget dokumentaldermetrikk for dokument med alder: {}", dagerGammel);
	}
}
