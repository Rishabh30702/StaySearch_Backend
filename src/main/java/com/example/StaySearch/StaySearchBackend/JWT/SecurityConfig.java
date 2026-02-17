package com.example.StaySearch.StaySearchBackend.JWT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

/* @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*"); // Allow all origins
        config.addAllowedMethod("*");        // Allow all methods
        config.addAllowedHeader("*");        // Allow all headers
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }*/

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(authProvider));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .headers(headers -> headers

                        // 🔒 HSTS
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                                .preload(true)
                        )

                        // 🖼 Clickjacking
                        .frameOptions(frame -> frame.sameOrigin())

                        // 🛡 Content Security Policy
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "frame-ancestors 'self'; " +
                                        "upgrade-insecure-requests"
                        ))

                        // 🧪 MIME sniffing (NON-deprecated)
                        .addHeaderWriter(new StaticHeadersWriter(
                                "X-Content-Type-Options", "nosniff"
                        ))

                        // 🔁 Referrer Policy (NON-deprecated)
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Referrer-Policy", "no-referrer-when-downgrade"
                        ))

                        // 🧠 Permissions Policy (NON-deprecated)
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy",
                                "geolocation=(), microphone=(), camera=(), notifications=(), midi=()"
                        ))
                )

                .authorizeHttpRequests(auth -> auth
                        // 1. ADMIN ONLY - High Priority
                        .requestMatchers("/auth/approve/hotelier/**").hasAnyAuthority("ADMIN", "Admin")
                        .requestMatchers("/auth/reject/**").hasAnyAuthority("ADMIN", "Admin")
                        .requestMatchers("/auth/pending/**").hasAnyAuthority("ADMIN", "Admin")
                        .requestMatchers("/auth/admin/**").hasAnyAuthority("ADMIN", "Admin")
                         .requestMatchers("/v1/deleteHotel/**", "/v1/hotels").hasAnyAuthority("ADMIN", "Admin")

                        .requestMatchers("/auth/allUsers", "/auth/delete/**").hasAnyAuthority("ADMIN", "Admin")
                        .requestMatchers(HttpMethod.GET,"/api/payments/invoice" ).hasAnyAuthority("ADMIN", "Admin")


                        // 2. HOTELIER & ADMIN - Middle Priority
                        .requestMatchers( "/v1/mine/hotels/**").hasAuthority("Hotelier")
                        .requestMatchers(HttpMethod.PATCH, "/v1/updateHotel/**").hasAnyAuthority("Hotelier", "ADMIN", "Admin")

                        // 3. AUTHENTICATED USERS
                        .requestMatchers("/auth/me/**", "/auth/wishlist/**").authenticated()


                        // 4. PUBLIC - Everything else
                        .anyRequest().permitAll()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "https://upstdcstaysearch.igiletechnologies.com",
                "https://upstdcstaysearch.com"
                // add localhost ONLY in dev profile
                // remove these when on testing env
//                 "http://localhost:4200"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
