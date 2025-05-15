package com.example.supervision;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()  // Active la configuration CORS que vous avez définie
            .csrf().disable()  // Désactive CSRF pour l'API REST (à adapter selon vos besoins de sécurité)
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()  // Toutes les requêtes nécessitent une authentification
            )
            .httpBasic();  // Utilise l'authentification HTTP Basic (à adapter selon votre système d'authentification)
            
        return http.build();
    }
}