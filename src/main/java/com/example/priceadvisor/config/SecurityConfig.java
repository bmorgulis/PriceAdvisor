package com.example.priceadvisor.config;

import com.example.priceadvisor.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    // Public pages
                    auth.requestMatchers("/sign-in", "/", "/index.html", "/terms-of-use", "/css/**", "/images/**", "/js/**").permitAll();

                    // Restricted access for MANAGER role only
                    auth.requestMatchers("/add-items", "/manage-accounts").hasRole("MANAGER");

                    // Any other page requires authentication
                    auth.anyRequest().authenticated();
                })
                .formLogin(form -> form
                        .loginPage("/sign-in")
                        .successHandler(customSuccessHandler()) // Custom success handler
                        .failureHandler(customFailureHandler()) // Custom failure handler
                        .permitAll()
                        .loginProcessingUrl("/login"))
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/sign-in?logout=true")
                        .permitAll())
                // Set access denied page without using deprecated exceptionHandling()
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied.html"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                    throws IOException {
                String targetUrl = determineTargetUrl(request);
                targetUrl += (targetUrl.contains("?") ? "&" : "?") + "login=true";
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            }

            private String determineTargetUrl(HttpServletRequest request) {
                var savedRequest = new HttpSessionRequestCache().getRequest(request, null);
                if (savedRequest != null) {
                    return savedRequest.getRedirectUrl();
                }
                return "/";
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
                    throws IOException {

                String loginErrorMessage = exception.getMessage().toLowerCase();

                // Customize the error message based on the exception type
                if (loginErrorMessage.contains("bad credentials")) {
                    loginErrorMessage = "Email and/or password incorrect.";
                } else {
                    loginErrorMessage = "An unexpected error occurred. Please try again.";
                }

                // Store the error message in session
                request.getSession().setAttribute("loginErrorMessage", loginErrorMessage);

                // Redirect back to the login page with the error message
                getRedirectStrategy().sendRedirect(request, response, "/sign-in");
            }
        };
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, BCryptPasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build();
    }
}
