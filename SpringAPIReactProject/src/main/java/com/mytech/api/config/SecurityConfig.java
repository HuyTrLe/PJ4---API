package com.mytech.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mytech.api.auth.jwt.AuthEntryPointJwt;
import com.mytech.api.auth.jwt.AuthTokenFilter;
import com.mytech.api.auth.oauth2.CustomOAuth2UserService;
import com.mytech.api.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.mytech.api.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.mytech.api.auth.services.UserDetailServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	@Autowired
	UserDetailServiceImpl userDetailServiceImpl;

	@Autowired
	AuthEntryPointJwt authEntryPointJwt;

	@Autowired
	CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@Autowired
	OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

	@Bean
	AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailServiceImpl);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
						.requestMatchers("/api/recurrences/**").permitAll()
						.requestMatchers("/api/categories/**").permitAll()
						.requestMatchers("/api/bills/**").permitAll()
						.requestMatchers("/api/wallet_types/**").permitAll()
						.requestMatchers("/api/wallets/**").permitAll()
						.requestMatchers("/api/expenses/**").permitAll()
						.requestMatchers("/api/debts/**").permitAll()
						.requestMatchers("/api/savinggoals/**").permitAll()
						.requestMatchers("/api/incomes/**").permitAll()
						.requestMatchers("/api/budgets/**").permitAll()
						.requestMatchers("/api/notifications/**").permitAll()
						.requestMatchers("/api/transactions/**").permitAll()
						.requestMatchers("/ws/**").permitAll()
						.requestMatchers("/api/transactionsRecurring/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						.requestMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg",
								"/**/*.jpg",
								"/**/*.html", "/**/*.css", "/**/*.js")
						.permitAll()
						.anyRequest().authenticated())
				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(authorization -> authorization
								.baseUri("/oauth2/authorize"))
						.redirectionEndpoint(redirection -> redirection
								.baseUri("/oauth2/callback/*"))
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService))
						.successHandler(oAuth2AuthenticationSuccessHandler)
						.failureHandler(oAuth2AuthenticationFailureHandler))
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authEntryPointJwt));
		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}