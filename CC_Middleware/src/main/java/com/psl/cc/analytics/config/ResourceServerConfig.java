package com.psl.cc.analytics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		//-- define URL patterns to enable OAuth2 security
		http.
		anonymous().disable()
		.requestMatchers().antMatchers("/api/**")
		.and().authorizeRequests()
		.antMatchers("/api/**").access("hasRole('ADMIN') or hasRole('USER') or hasRole('SYSADMIN')")
		.and().requestMatchers().antMatchers("/actuator/**").and().authorizeRequests().and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
		
//		http.
//		anonymous().disable()
//		.requestMatchers().antMatchers("/actuator/**")
//		.and().authorizeRequests();
	}
	
}