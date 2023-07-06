package no.nav.safselvbetjening.fullmektig;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FullmektigService {
	private final FullmektigConsumer fullmektigConsumer;

	public FullmektigService(FullmektigConsumer fullmektigConsumer) {
		this.fullmektigConsumer = fullmektigConsumer;
	}

	public Optional<Fullmakt> fullmektig(String fullmektigSubjectToken, String fullmaktsgiver) {
		List<FullmektigTemaResponse> fullmektigTema = fullmektigConsumer.fullmektigTema(fullmektigSubjectToken);

		if (fullmektigTema.isEmpty()) {
			return Optional.empty();
		}
		return fullmektigTema.stream().filter(ft -> fullmaktsgiver.equals(ft.fullmaktsgiver())).map(ft -> new Fullmakt(new ArrayList<>(ft.tema()))).findAny();
	}
}
