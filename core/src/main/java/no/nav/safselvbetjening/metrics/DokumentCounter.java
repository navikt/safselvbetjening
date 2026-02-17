package no.nav.safselvbetjening.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DokumentCounter {
//	private final MeterRegistry meterRegistry;
	private final DistributionSummary alderSummary;

	@Autowired
	public DokumentCounter(MeterRegistry meterRegistry) {
		this.alderSummary = DistributionSummary.builder("dok_safselvbetjening_dokument_alder2")
				.description("Alder på dokumenter i dager")
				.serviceLevelObjectives(
						30,   // 0–30
						180,  // 31–180
						365,  // 181–365
						730,  // 366–730
						1825  // 731–1825
				)
				.register(meterRegistry);
	}

	public void registerAlder(double alder){
		log.info("Logger dokumentaldermetrikk for dokument med alder: {}", alder);
		alderSummary.record(alder);
	}
}
