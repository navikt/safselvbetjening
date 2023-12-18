package no.nav.safselvbetjening.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatcher(
						"/actuator", // helsesjekk og metrikker - åpent
						"/graphql", // beskyttet av token-support @Protected
						"/rest" // beskyttet av token-support @Protected
				)
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.csrf(AbstractHttpConfigurer::disable)
				.cors(AbstractHttpConfigurer::disable)
				.build();
	}
}
