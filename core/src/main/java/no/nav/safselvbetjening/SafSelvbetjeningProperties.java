package no.nav.safselvbetjening;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

}
