package no.nav.safselvbetjening.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DokumentCounter {
	private final MeterRegistry meterRegistry;

	@Autowired
	public DokumentCounter(MeterRegistry meterRegistry) { this.meterRegistry = meterRegistry; }

	public void increment(String alder){
		Counter.builder("dok_safselvbetjening_dokument_alder")
				.tags("alder", alder)
				.register(meterRegistry)
				.increment();
	}
}
