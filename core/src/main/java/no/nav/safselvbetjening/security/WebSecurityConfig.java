package no.nav.safselvbetjening.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.requestMatchers(EndpointRequest.to("isAlive", "isReady", "actuator")).permitAll()
				.and()
				// Håndteres av token-support interceptor
				.authorizeRequests().antMatchers(HttpMethod.POST, "/graphql").permitAll()
				.and().csrf().disable();
	}
}