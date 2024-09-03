package no.nav.safselvbetjening.dokumentoversikt;

import lombok.extern.slf4j.Slf4j;
import no.nav.safselvbetjening.SafeLoggingUtil;
import no.nav.safselvbetjening.consumer.dokarkiv.Basedata;
import no.nav.safselvbetjening.consumer.dokarkiv.Saker;
import no.nav.safselvbetjening.domain.Sakstema;
import no.nav.safselvbetjening.domain.Tema;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static no.nav.safselvbetjening.domain.Tema.UKJ;

@Slf4j
@Component
public class TemaQueryService {

	List<Sakstema> query(final Basedata basedata) {
		log.info("dokumentoversiktSelvbetjening henter /tema.");
		final Saker saker = basedata.saker();
		List<Sakstema> sakstema = saker.getArkivsakerTemaer()
				.distinct()
				.map(TemaQueryService::mapSakstema)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Sakstema::getKode))
				.toList();
		log.info("dokumentoversiktSelvbetjening hentet /tema. antall_tema={}.", sakstema.size());
		return sakstema;
	}

	private static Sakstema mapSakstema(String arkivsakTema) {
		final Tema tema = determineTema(arkivsakTema);
		if (Tema.unntattInnsynNavNo().contains(tema)) {
			return null;
		}
		return Sakstema.builder()
				.kode(tema.name())
				.navn(tema.getTemanavn())
				.build();
	}

	private static Tema determineTema(String arkivsakTema) {
		try {
			return Tema.valueOf(arkivsakTema);
		} catch (IllegalArgumentException e) {
			log.error("Mapping av tema={} feilet. Dette må rettes.", SafeLoggingUtil.removeUnsafeChars(arkivsakTema));
			return UKJ;
		}
	}
}
