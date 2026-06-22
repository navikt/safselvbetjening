package no.nav.safselvbetjening.representasjon;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public sealed interface Representasjonsforhold extends Comparable<Representasjonsforhold> permits Fullmakt, Vergemaal {
	/// @return Sett over tema
	Set<String> tema();

	/// Om tema gjelder for representasjonen
	///
	/// @param tema Nav arkivtema
	/// @return `true` hvis representant har innsyn for `tema`. Ellers `false`
	default boolean gjelderForTema(String tema) {
		return this.tema().contains(tema);
	}

	/// Sorterer Vergemaal før Fullmakt
	@Override
	default int compareTo(@NonNull Representasjonsforhold o) {
		if (this.getClass() == o.getClass()) {
			return 0;
		} else if (this instanceof Vergemaal) {
			return 1;
		} else {
			return -1;
		}
	}

	static Set<String> alleTema(List<Representasjonsforhold> representasjon) {
		return representasjon.stream()
				.flatMap(r -> r.tema().stream())
				.collect(toSet());
	}
}
