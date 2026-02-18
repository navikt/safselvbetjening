package no.nav.safselvbetjening.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class DokumentCounter {
	private final Counter bucket0_6;
	private final Counter bucket7_12;
	private final Counter bucket13_24;
	private final Counter bucket25_60;
	private final Counter bucket60plus;
	private final DistributionSummary alderSummary;


	public DokumentCounter(MeterRegistry meterRegistry) {
		bucket0_6 = meterRegistry.counter("dok_alder_months_bucket", "range", "A:0-6");
		bucket7_12 = meterRegistry.counter("dok_alder_months_bucket", "range", "B:7-12");
		bucket13_24 = meterRegistry.counter("dok_alder_months_bucket", "range", "C:13-24");
		bucket25_60 = meterRegistry.counter("dok_alder_months_bucket", "range", "D:25-60");
		bucket60plus = meterRegistry.counter("dok_alder_months_bucket", "range", "E:60plus");
		this.alderSummary = DistributionSummary.builder("dok_safselvbetjening_dokument_alder3")
				.description("Alder på dokumenter i dager")
				.scale(100)
				.serviceLevelObjectives(6, 12, 24, 60)
				.register(meterRegistry);

	}

	public void registrerAlderDokumentMetrikk(OffsetDateTime datoOpprettet) {
		long maanederGammel = ChronoUnit.MONTHS.between(datoOpprettet, OffsetDateTime.now());
		alderSummary.record(maanederGammel);

		if (maanederGammel <= 6) bucket0_6.increment();
		else if (maanederGammel <= 12) bucket7_12.increment();
		else if (maanederGammel <= 24) bucket13_24.increment();
		else if (maanederGammel <= 60) bucket25_60.increment();
		else bucket60plus.increment();
	}
}
