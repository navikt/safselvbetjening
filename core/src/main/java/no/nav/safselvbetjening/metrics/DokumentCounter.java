package no.nav.safselvbetjening.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DokumentCounter {
	private final MeterRegistry meterRegistry;

	@Autowired
	public DokumentCounter(MeterRegistry meterRegistry) { this.meterRegistry = meterRegistry; }

	public void increment(String alder){
		log.info("Logger dokumentaldermetrikk");
		Counter.builder("dok_safselvbetjening_dokument_alder")
				.tags("alder", alder)
				.register(meterRegistry)
				.increment();
	}
}
