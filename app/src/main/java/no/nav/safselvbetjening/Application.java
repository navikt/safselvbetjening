package no.nav.safselvbetjening;

import no.nav.safselvbetjening.nais.NaisContract;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        ApplicationConfig.class,
        NaisContract.class
})
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
