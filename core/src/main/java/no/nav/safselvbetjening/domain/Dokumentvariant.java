package no.nav.safselvbetjening.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Builder
@Data
public class Dokumentvariant {
	private final Variantformat variantformat;
	private final String filuuid;
	private final String filtype;
	private final int filstorrelse;
	private boolean brukerHarTilgang;
	@Builder.Default
	private List<String> code = new ArrayList<>();

}
