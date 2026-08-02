package com.example.commitmate.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Mustache에는 Thymeleaf 보안 dialect 같은 지연 토큰 처리가 없으므로
                // 요청마다 토큰을 즉시(eager) 확정하는 핸들러를 사용한다.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/app/**")
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/security-logout")
                        .disable()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/login/oauth2/google", true)
                );
        return http.build();
    }
}
