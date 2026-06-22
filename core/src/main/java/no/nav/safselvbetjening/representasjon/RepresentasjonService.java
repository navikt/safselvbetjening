package no.nav.safselvbetjening.representasjon;

import no.nav.safselvbetjening.representasjon.api.RepresentasjonsforholdDto;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class RepresentasjonService {
	private final RepresentasjonConsumer representasjonConsumer;

	public RepresentasjonService(RepresentasjonConsumer representasjonConsumer) {
		this.representasjonConsumer = representasjonConsumer;
	}

	public Optional<Representasjon> finnRepresentasjon(JwtToken representantJwt, String representertIdent) {
		return utledRepresentasjon(representantJwt, representertIdent);
	}

	private Optional<Representasjon> utledRepresentasjon(JwtToken representantJwt, String representertIdent) {
		RepresentasjonsforholdDto representasjonsforholdDto = representasjonConsumer.representasjonsForhold(representantJwt.getEncodedToken());

		if (representasjonsforholdDto.isEmpty()) {
			return Optional.empty();
		}

		List<Representasjonsforhold> representasjonsforhold = Stream.concat(
				representasjonsforholdDto.fullmakt().stream()
						.filter(fm -> representertIdent.equals(fm.fullmaktsgiver()))
						.filter(fm -> !fm.tema().isEmpty())
						.map(fm -> (Representasjonsforhold) new Fullmakt(fm.fullmektig(), fm.fullmaktsgiver(), fm.tema())),
				representasjonsforholdDto.vergemaal().stream()
						.filter(vm -> representertIdent.equals(vm.vergehaver()))
						.filter(vm -> !vm.tema().isEmpty())
						.map(vm -> (Representasjonsforhold) new Vergemaal(vm.verge(), vm.vergehaver(), vm.tema()))
		).toList();

		if (representasjonsforhold.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new Representasjon(representasjonsforhold));
	}
}
