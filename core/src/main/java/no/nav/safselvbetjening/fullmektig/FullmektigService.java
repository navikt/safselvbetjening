package no.nav.safselvbetjening.fullmektig;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FullmektigService {
	private final FullmektigConsumer fullmektigConsumer;

	public FullmektigService(FullmektigConsumer fullmektigConsumer) {
		this.fullmektigConsumer = fullmektigConsumer;
	}

	public Optional<Fullmakt> fullmektig(String fullmektigSubjectToken, String fullmaktsgiver) {
		List<FullmaktDetails> fullmakter = fullmektigConsumer.fullmektig(fullmektigSubjectToken);
		String aktiveTemaForFullmakt = fullmakter.stream().filter(f -> erFullmaktGyldig(fullmaktsgiver, f))
				.map(FullmaktDetails::omraade)
				.collect(Collectors.joining(";"));
		Set<String> tema = new HashSet<>(Arrays.stream(aktiveTemaForFullmakt.split(";")).toList());
		if(tema.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(new Fullmakt(tema.stream().toList()));
		}
	}

	private static boolean erFullmaktGyldig(String fullmaktsgiver, FullmaktDetails f) {
		LocalDate idag = LocalDate.now();
		return f.fullmaktsgiver().equals(fullmaktsgiver)
			   && (!idag.isBefore(f.gyldigFraOgMed()) && idag.isBefore(f.gyldigTilOgMed()));
	}
}
