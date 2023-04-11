package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import no.nav.safselvbetjening.service.Arkivsak;
import no.nav.safselvbetjening.service.Saker;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static no.nav.safselvbetjening.domain.Tema.UKJ;

@Slf4j
@Component
class TemaQueryService {
	private static final EnumSet<Tema> TEMA_IKKE_INNSYN_FOR_BRUKER = Tema.brukerHarIkkeInnsyn();

	List<Sakstema> query(final Basedata basedata) {
		log.info("dokumentoversiktSelvbetjening henter /tema.");
		final Saker saker = basedata.getSaker();
		List<Sakstema> sakstema = saker.getArkivsakerAsStream()
				.filter(distinctByKey(Arkivsak::getTema))
				.map(this::mapSakstema)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Sakstema::getKode))
				.collect(Collectors.toList());
		log.info("dokumentoversiktSelvbetjening hentet /tema. antall_tema={}.", sakstema.size());
		return sakstema;
	}

	private Sakstema mapSakstema(Arkivsak arkivsak) {
		final Tema tema = determineTema(arkivsak);
		if (TEMA_IKKE_INNSYN_FOR_BRUKER.contains(tema)) {
			return null;
		}
		return Sakstema.builder()
				.kode(tema.name())
				.navn(tema.getTemanavn())
				.build();
	}

	private Tema determineTema(Arkivsak arkivsak) {
		try {
			return Tema.valueOf(arkivsak.getTema());
		} catch (IllegalArgumentException e) {
			log.error("Mapping av tema={} feilet. Dette må rettes.", arkivsak.getTema());
			return UKJ;
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), TRUE) == null;
	}
}
