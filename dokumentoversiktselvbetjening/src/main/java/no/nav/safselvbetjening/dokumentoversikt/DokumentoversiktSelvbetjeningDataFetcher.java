package no.nav.safselvbetjening.dokumentoversikt;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktSelvbetjeningDataFetcher implements DataFetcher<Object> {
    private static final List<String> ALLE_TEMA = Arrays.asList("AAP", "AAR", "AGR", "BAR", "BID", "BIL", "DAG", "ENF",
            "ERS", "FAR", "FEI", "FOR", "FOS", "FRI", "FUL", "GEN", "GRA", "GRU", "HEL", "HJE", "IAR", "IND", "KON",
            "KTR", "MED", "MOB", "OMS", "OPA", "OPP", "PEN", "PER", "REH", "REK", "RPO", "RVE", "SAA", "SAK", "SAP",
            "SER", "SIK", "STO", "SUP", "SYK", "SYM", "TIL", "TRK", "TRY", "TSO", "TSR", "UFM", "UFO", "UKJ", "VEN",
            "YRA", "YRK");

    private final DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService;

    public DokumentoversiktSelvbetjeningDataFetcher(DokumentoversiktSelvbetjeningService dokumentoversiktSelvbetjeningService) {
        this.dokumentoversiktSelvbetjeningService = dokumentoversiktSelvbetjeningService;
    }

    @Override
    public Object get(DataFetchingEnvironment dataFetchingEnvironment) throws Exception {

        return dokumentoversiktSelvbetjeningService.queryDokumentoversikt(
                dataFetchingEnvironment.getArgumentOrDefault("ident", null),
                temaArgument(dataFetchingEnvironment));
    }


    private List<String> temaArgument(DataFetchingEnvironment environment) {
        final List<String> tema = environment.getArgumentOrDefault("tema", new ArrayList<>());
        return tema.isEmpty() ? ALLE_TEMA : tema;
    }
}
