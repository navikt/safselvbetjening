package no.nav.safselvbetjening.representasjon;

import no.nav.safselvbetjening.representasjon.api.RepresentasjonsforholdDto;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class RepresentasjonService {
	private final RepresentasjonConsumer representasjonConsumer;

	public RepresentasjonService(RepresentasjonConsumer representasjonConsumer) {
		this.representasjonConsumer = representasjonConsumer;
	}

	public Optional<Representasjon> finnRepresentasjon(JwtToken representantJwt, String representertIdent) {
		RepresentasjonsforholdDto representasjonsforholdDto = representasjonConsumer.representasjonsForhold(representantJwt.getEncodedToken());

		if (representasjonsforholdDto.isEmpty()) {
			return Optional.empty();
		}

		String representantIdent = extractRepresentantIdent(representantJwt);
		List<Representasjonsforhold> representasjonsforhold = Stream.concat(
				representasjonsforholdDto.fullmakt().stream()
						.filter(fm -> representantIdent.equals(fm.fullmektig()))
						.filter(fm -> representertIdent.equals(fm.fullmaktsgiver()))
						.filter(fm -> !fm.tema().isEmpty())
						.map(fm -> (Representasjonsforhold) new Fullmakt(fm.fullmektig(), fm.fullmaktsgiver(), fm.tema())),
				representasjonsforholdDto.vergemaal().stream()
						.filter(vm -> representantIdent.equals(vm.verge()))
						.filter(vm -> representertIdent.equals(vm.vergehaver()))
						.filter(vm -> !vm.tema().isEmpty())
						.map(vm -> (Representasjonsforhold) new Vergemaal(vm.verge(), vm.vergehaver(), vm.tema()))
		).toList();

		if (representasjonsforhold.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new Representasjon(representasjonsforhold));
	}

	String extractRepresentantIdent(JwtToken jwtToken) {
		String pidClaim = jwtToken.getJwtTokenClaims().getStringClaim(CLAIM_PID);
		if (isNotBlank(pidClaim)) {
			return pidClaim;
		}
		String subClaim = jwtToken.getJwtTokenClaims().getStringClaim(CLAIM_SUB);
		if (isNotBlank(subClaim)) {
			return subClaim;
		}
		throw new IllegalArgumentException("Tillater ikke oppslag av fullmektig uten pid/sub claim i token til innlogget bruker");
	}
}
