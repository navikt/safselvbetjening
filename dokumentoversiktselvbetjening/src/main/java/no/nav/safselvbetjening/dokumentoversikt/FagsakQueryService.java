package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Fagsak;
import no.nav.safselvbetjening.service.Arkivsak;
import no.nav.safselvbetjening.service.Saker;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class FagsakQueryService {
	List<Fagsak> query(final Basedata basedata) {
		log.info("dokumentoversiktSelvbetjening henter /fagsak.");
		final Saker saker = basedata.getSaker();
		List<Fagsak> fagsaker = saker.getArkivsakerAsStream()
				.filter(Arkivsak::isFagsak)
				.filter(distinctByKey(Arkivsak::getFagSakIdAndFagsaksystem))
				.map(this::mapFagsak)
				.sorted(Comparator.comparing(Fagsak::getFagsaksystem))
				.collect(Collectors.toList());
		log.info("dokumentoversiktSelvbetjening hentet /fagsak. antall_fagsaker={}.", fagsaker.size());
		return fagsaker;
	}

	private Fagsak mapFagsak(final Arkivsak arkivsak) {
		return Fagsak.builder()
				.fagsakId(arkivsak.getFagsakId())
				.fagsaksystem(arkivsak.getFagsaksystem())
				.tema(arkivsak.getTema())
				.build();
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), TRUE) == null;
	}
}
