package no.nav.safselvbetjening.fullmektig;

import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.safselvbetjening.TokenClaims.CLAIM_PID;
import static no.nav.safselvbetjening.TokenClaims.CLAIM_SUB;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class FullmektigService {
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	private final FullmektigV2Consumer fullmektigConsumer;

	public FullmektigService(FullmektigV2Consumer fullmektigConsumer) {
		this.fullmektigConsumer = fullmektigConsumer;
	}

	public Optional<Fullmakt> finnFullmakt(JwtToken subjectJwt, String fullmaktsgiverIdent) {
		String fullmektigIdent = extractFullmektigIdent(subjectJwt);

		secureLog.info("innloggetbruker(ident={}), fullmaktsgiver(ident={}) Ser etter fullmakter mellom innlogget bruker og fullmaktsgiver", fullmektigIdent, fullmaktsgiverIdent);
		Optional<Fullmakt> fullmakt = utledFullmakt(subjectJwt, fullmektigIdent, fullmaktsgiverIdent);
		if (fullmakt.isPresent()) {
			secureLog.info("fullmektig(ident={}), fullmaktsgiver(ident={}) Bruker fullmakt mellom fullmektig og fullmaktsgiver. Returnerer ressurser med tema={}", fullmektigIdent, fullmaktsgiverIdent, fullmakt.get().tema());
		} else {
			secureLog.warn("innloggetbruker(ident={}), fullmaktsgiver(ident={}) Ingen fullmakter mellom innlogget bruker og fullmaktsgiver", fullmektigIdent, fullmaktsgiverIdent);
		}
		return fullmakt;
	}

	private Optional<Fullmakt> utledFullmakt(JwtToken subjectJwt, String fullmektigIdent, String fullmaktsgiverIdent) {
		List<KanRepresentereDetaljertTemaResponse> fullmaktPaaTema = fullmektigConsumer.fullmektigTema(subjectJwt.getEncodedToken());

		if (fullmaktPaaTema.isEmpty()) {
			return Optional.empty();
		}
		return fullmaktPaaTema.stream()
				.filter(ft -> fullmaktsgiverIdent.equals(ft.fullmaktsgiver()))
				.filter(ft -> !ft.tema().isEmpty())
				.map(ft -> new Fullmakt(ft.fullmektig(), ft.fullmaktsgiver(), new ArrayList<>(ft.tema()))).findAny();
	}

	String extractFullmektigIdent(JwtToken jwtToken) {
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
