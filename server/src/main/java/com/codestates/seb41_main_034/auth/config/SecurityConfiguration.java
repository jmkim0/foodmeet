package com.codestates.seb41_main_034.auth.config;

import com.codestates.seb41_main_034.auth.handler.UserAccessDeniedHandler;
import com.codestates.seb41_main_034.auth.handler.UserAuthenticationEntryPoint;
import com.codestates.seb41_main_034.auth.jwt.JwtTokenizer;
import com.codestates.seb41_main_034.auth.userdetails.CustomUserDetailsService;
import com.codestates.seb41_main_034.auth.utils.CustomAuthorityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper mapper;

//    @Value("${spring.security.oauth2.client.registration.google.clientId}")
//    private String clientId;
//
//    @Value("${spring.security.oauth2.client.registration.google.clientSecret}")
//    private String clientSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(sessionManagement -> {
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.authenticationEntryPoint(new UserAuthenticationEntryPoint(mapper))
                            .accessDeniedHandler(new UserAccessDeniedHandler(mapper));
                })
                .apply(new CustomFilterConfigurer(jwtTokenizer, authorityUtils, customUserDetailsService, mapper))
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/user/duplicate-check")).permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/api/v1/product"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/product/*"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/ordering/*/prepare"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/ordering/*/ship"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/ordering/*/confirm-cancellation"),
                                antMatcher(HttpMethod.POST, "/api/v1/question/*/answer"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/question/*/answer")
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/api/v1/user/*"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/user/*"),
                                antMatcher(HttpMethod.DELETE, "/api/v1/user/*"),
                                antMatcher(HttpMethod.POST, "/api/v1/user-address"),
                                antMatcher(HttpMethod.GET, "/api/v1/user-address"),
                                antMatcher(HttpMethod.GET, "/api/v1/user-address/*"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/user-address/*"),
                                antMatcher(HttpMethod.DELETE, "/api/v1/user-address/*"),
                                antMatcher(HttpMethod.POST, "/api/v1/cart"),
                                antMatcher(HttpMethod.GET, "/api/v1/cart"),
                                antMatcher(HttpMethod.GET, "/api/v1/cart/*"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/cart/*"),
                                antMatcher(HttpMethod.DELETE, "/api/v1/cart/*"),
                                antMatcher(HttpMethod.GET, "/api/v1/order/*"),
                                antMatcher(HttpMethod.POST, "/api/v1/ordering"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/ordering/**"),
                                antMatcher(HttpMethod.POST, "/api/v1/question"),
                                antMatcher(HttpMethod.GET, "/api/v1/question/question-history"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/question/*"),
                                antMatcher(HttpMethod.DELETE, "/api/v1/question/*"),
                                antMatcher(HttpMethod.POST, "/api/v1/review"),
                                antMatcher(HttpMethod.GET, "/api/v1/review/review-history"),
                                antMatcher(HttpMethod.PATCH, "/api/v1/review/*"),
                                antMatcher(HttpMethod.DELETE, "/api/v1/review/*")
                        ).hasRole("USER")
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "refreshToken"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
