package no.nav.safselvbetjening.tilgang;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static no.nav.safselvbetjening.MDCUtils.MDC_FULLMAKT_TEMA;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.safselvbetjening.fullmektig.Fullmakt;
import no.nav.safselvbetjening.fullmektig.FullmektigService;
import no.nav.safselvbetjening.service.BrukerIdenter;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class AccessValidationUtil {
	private final FullmektigService fullmektigService;

	public AccessValidationUtil(FullmektigService fullmektigService) {
		this.fullmektigService = fullmektigService;
	}

	public Optional<Fullmakt> validerInnloggetBrukerOgFinnFullmakt(
			BrukerIdenter brukerIdenter,
			TokenValidationContext tokenValidationContext,
			BiConsumer<String, List<String>> userNotPresentAndFullmaktInvalid
	) throws NoValidTokensException {
		JwtToken subjectJwt = tokenValidationContext.getFirstValidToken();
		if (subjectJwt == null) {
			throw new NoValidTokensException();
		}
		List<String> identer = brukerIdenter.getIdenter().stream().map(Ident::get).toList();
		String pid = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_PID);
		String sub = subjectJwt.getJwtTokenClaims().getStringClaim(CLAIM_SUB);
		if (!identer.contains(pid) && !identer.contains(sub)) {
			Optional<Fullmakt> fullmakt = fullmektigService.finnFullmakt(subjectJwt, brukerIdenter.getAktivFolkeregisterident());
			if (fullmakt.isPresent()) {
				MDC.put(MDC_FULLMAKT_TEMA, fullmakt.get().tema().toString());
				return fullmakt;
			} else {
				userNotPresentAndFullmaktInvalid.accept(pidOrSub(pid, sub), identer);
			}
		}
		return Optional.empty();
	}

	public static void validerFullmakt(Optional<Fullmakt> fullmaktOpt, String gjeldendeTema,
									   Consumer<Fullmakt> presentAndValid, Consumer<Fullmakt> presentAndInvalid) {
		fullmaktOpt.ifPresent(fullmakt -> {
			if (fullmakt.gjelderForTema(gjeldendeTema)) {
				presentAndValid.accept(fullmakt);
			} else {
				presentAndInvalid.accept(fullmakt);
			}
		});
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
