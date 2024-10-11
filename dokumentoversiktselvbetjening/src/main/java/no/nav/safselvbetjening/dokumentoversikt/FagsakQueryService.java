package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.consumer.dokarkiv.Basedata;
import no.nav.safselvbetjening.domain.Fagsak;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Boolean.TRUE;

@Slf4j
@Component
public class FagsakQueryService {
	List<Fagsak> query(Basedata basedata) {
		log.info("dokumentoversiktSelvbetjening henter /fagsak.");
		List<Fagsak> fagsaker = basedata.saker()
				.getFagsaker()
				.filter(distinctByKey(Fagsak::getFagSakIdAndFagsaksystem))
				.sorted(Comparator.comparing(Fagsak::getFagsaksystem))
				.toList();
		log.info("dokumentoversiktSelvbetjening hentet /fagsak. antall_fagsaker={}.", fagsaker.size());
		return fagsaker;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), TRUE) == null;
	}
}
