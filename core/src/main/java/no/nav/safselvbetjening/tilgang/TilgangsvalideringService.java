package no.nav.safselvbetjening.tilgang;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.fullmektig.FullmektigService;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static no.nav.safselvbetjening.MDCUtils.MDC_FULLMAKT_TEMA;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class TilgangsvalideringService {
	private final FullmektigService fullmektigService;

	public TilgangsvalideringService(FullmektigService fullmektigService) {
		this.fullmektigService = fullmektigService;
	}

	public Optional<Fullmakt> validerInnloggetBrukerOgFinnFullmakt(
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
			Optional<Fullmakt> fullmakt = fullmektigService.finnFullmakt(subjectJwt, brukerIdenter.getAktivFolkeregisterident());
			if (fullmakt.isPresent()) {
				MDC.put(MDC_FULLMAKT_TEMA, fullmakt.get().tema().toString());
				return fullmakt;
			} else {
				throw new UserNotMatchingTokenException(pidOrSub(pid, sub), identer);
			}
		}
		return Optional.empty();
	}

	public static void validerFullmaktForTema(Fullmakt fullmakt, String gjeldendeTema, Consumer<Fullmakt> presentAndValid) {
		if (fullmakt.gjelderForTema(gjeldendeTema)) {
			presentAndValid.accept(fullmakt);
		} else {
			throw new FullmaktInvalidException(fullmakt, gjeldendeTema);
		}
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
