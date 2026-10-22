package com.smartpoweron.gestion_materiales.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Archivos estáticos libres
                .anyRequest().authenticated() // Todo lo demás requiere iniciar sesión
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails almacenero = User.withDefaultPasswordEncoder()
            .username("almacen")
            .password("1234")
            .roles("ALMACENERO")
            .build();

        UserDetails produccion = User.withDefaultPasswordEncoder()
            .username("produccion")
            .password("1234")
            .roles("PRODUCCION")
            .build();

        UserDetails calidad = User.withDefaultPasswordEncoder()
            .username("calidad")
            .password("1234")
            .roles("CALIDAD")
            .build();

        UserDetails tecnico = User.withDefaultPasswordEncoder()
            .username("tecnico")
            .password("1234")
            .roles("TECNICO")
            .build();

        return new InMemoryUserDetailsManager(almacenero, produccion, calidad, tecnico);
    }
}