package no.nav.safselvbetjening.tilgang;

import no.nav.safselvbetjening.representasjon.Representasjon;
import no.nav.safselvbetjening.representasjon.RepresentasjonService;
import no.nav.safselvbetjening.representasjon.Representasjonsforhold;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class TilgangsvalideringService {
	private final RepresentasjonService representasjonService;

	public TilgangsvalideringService(RepresentasjonService representasjonService) {
		this.representasjonService = representasjonService;
	}

	public Optional<Representasjon> validerInnloggetBrukerOgFinnRepresentasjon(
			BrukerIdenter brukerIdenter,
			TokenValidationContext tokenValidationContext
	) throws NoValidTokensException {
		JwtToken subjectJwt = tokenValidationContext.getFirstValidToken();
		if (subjectJwt == null) {
			throw new NoValidTokensException();
		}
		Set<String> identer = brukerIdenter.getIdenter().stream().map(Ident::get).collect(Collectors.toSet());
		String pid = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_PID);
		String sub = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_SUB);
		if (!identer.contains(pid) && !identer.contains(sub)) {
			Optional<Representasjon> representasjon = representasjonService.finnRepresentasjon(subjectJwt, brukerIdenter.getAktivFolkeregisterident());
			if (representasjon.isPresent()) {
				return representasjon;
			} else {
				throw new UserNotMatchingTokenException(pidOrSub(pid, sub), identer);
			}
		}
		return Optional.empty();
	}

	public static Representasjonsforhold validerRepresentasjonForTema(Representasjon representasjon, String gjeldendeTema, Consumer<Representasjonsforhold> presentAndValid) {
		Optional<Representasjonsforhold> match = representasjon.vergemaal()
				.filter(v -> v.gjelderForTema(gjeldendeTema))
				.<Representasjonsforhold>map(v -> v)
				.or(() -> representasjon.fullmakt()
						.filter(f -> f.gjelderForTema(gjeldendeTema))
						.<Representasjonsforhold>map(f -> f));

		if (match.isPresent()) {
			Representasjonsforhold valgtRepresentasjon = match.get();
			presentAndValid.accept(valgtRepresentasjon);
			return valgtRepresentasjon;
		}
		throw new RepresentasjonInvalidException(representasjon.select(), gjeldendeTema);
	}

	public String getPidOrSubFromRequest(TokenValidationContext tokenValidationContext) {
		var jwtToken = tokenValidationContext.getFirstValidToken();
		if (jwtToken != null) {
			var jwtTokenClaims = jwtToken.getJwtTokenClaims();
			return pidOrSub(jwtTokenClaims.getStringClaim("pid"), jwtTokenClaims.getStringClaim("sub"));
		}
		return null;
	}

	private String pidOrSub(String pid, String sub) {
		if (isNotBlank(pid)) {
			return pid;
		}
		if (isNotBlank(sub)) {
			return sub;
		}
		return null;
	}
}
