package im.jmk.foodmeet.auth.config;

import im.jmk.foodmeet.auth.filter.JwtAuthenticationFilter;
import im.jmk.foodmeet.auth.filter.JwtVerificationFilter;
import im.jmk.foodmeet.auth.handler.UserAccessDeniedHandler;
import im.jmk.foodmeet.auth.handler.UserAuthenticationEntryPoint;
import im.jmk.foodmeet.auth.handler.UserAuthenticationFailureHandler;
import im.jmk.foodmeet.auth.handler.UserAuthenticationSuccessHandler;
import im.jmk.foodmeet.auth.jwt.JwtTokenizer;
import im.jmk.foodmeet.auth.userdetails.CustomUserDetailsService;
import im.jmk.foodmeet.auth.utils.CustomAuthorityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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

    // TODO: OAuth2 구현

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(http.getSharedObject(AuthenticationManager.class), jwtTokenizer, mapper);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/v1/user/login");
        jwtAuthenticationFilter.setAuthenticationSuccessHandler(new UserAuthenticationSuccessHandler());
        jwtAuthenticationFilter.setAuthenticationFailureHandler(new UserAuthenticationFailureHandler(mapper));

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
                .addFilter(jwtAuthenticationFilter)
                .addFilterAfter(
                        new JwtVerificationFilter(jwtTokenizer, authorityUtils, customUserDetailsService),
                        JwtAuthenticationFilter.class
                )
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
