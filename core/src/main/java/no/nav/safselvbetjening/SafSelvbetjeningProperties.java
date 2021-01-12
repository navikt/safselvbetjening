package no.nav.safselvbetjening;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("safselvbetjening")
@Validated
public class SafSelvbetjeningProperties {

}
