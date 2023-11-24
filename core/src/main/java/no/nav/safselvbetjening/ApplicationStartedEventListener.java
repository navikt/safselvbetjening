package no.nav.safselvbetjening;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Hooks;

public class ApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		registerReactorContextPropagation();
	}

	private static void registerReactorContextPropagation() {
		Hooks.enableAutomaticContextPropagation();
		MDCUtils.ALL_KEYS.forEach(ApplicationStartedEventListener::registerMDCKey);
	}

	private static void registerMDCKey(String key) {
		ContextRegistry.getInstance().registerThreadLocalAccessor(
				key,
				() -> MDC.get(key),
				value -> MDC.put(key, value),
				() -> MDC.remove(key));
	}
}
