package no.nav.safselvbetjening.representasjon;

import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record Representasjon(@NonNull List<Representasjonsforhold> representasjonsforhold) {
	public Representasjon {
		if (representasjonsforhold.isEmpty()) {
			throw new IllegalArgumentException("representasjonsforhold kan ikke være null eller tom");
		}
	}

	public @NonNull Optional<Fullmakt> fullmakt() {
		return representasjonsforhold.stream()
				.filter(r -> r instanceof Fullmakt)
				.map(r -> (Fullmakt) r)
				.findFirst();
	}

	public @NonNull Optional<Vergemaal> vergemaal() {
		return representasjonsforhold.stream()
				.filter(r -> r instanceof Vergemaal)
				.map(r -> (Vergemaal) r)
				.findFirst();
	}

	public @NonNull Representasjonsforhold select() {
		return representasjonsforhold.stream()
				.max(Comparator.naturalOrder())
				.orElseThrow();
	}

	public @NonNull Set<String> alleTema() {
		return Representasjonsforhold.alleTema(representasjonsforhold);
	}
}
