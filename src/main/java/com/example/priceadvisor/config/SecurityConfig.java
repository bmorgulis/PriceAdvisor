package com.example.priceadvisor.config;

import com.example.priceadvisor.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            logger.info("Starting HTTP security configuration...");
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/", "/index.html", "/sign-in.html", "/css/**", "/images/**", "/js/**").permitAll();
                        auth.anyRequest().authenticated();
                    })
                    .formLogin(form -> form.loginPage("/sign-in.html")
                            .defaultSuccessUrl("/", false)
                            .permitAll()
                            .loginProcessingUrl("/login"))
                    .logout(logout -> logout.logoutUrl("/logout")
                            .logoutSuccessUrl("/sign-in.html?logout")
                            .permitAll());
            logger.info("HTTP Security Configured");
        } catch (Exception e) {
            logger.error("Error configuring HTTP Security: ", e);
        }

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, BCryptPasswordEncoder passwordEncoder) throws Exception {
        logger.debug("Creating AuthenticationManager...");

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        logger.debug("AuthenticationManager created");

        return authenticationManager;
    }
}
