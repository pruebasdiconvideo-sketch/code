package com.smartpoweron.gestion_materiales.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.security.session-timeout-minutes:30}")
    private int sessionTimeoutMinutes;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF: habilitado para formularios Thymeleaf, desactivado en /api/**
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/api/**")
            )
            // 2. Sesiones: ✅ CORREGIDO: Se eliminó el .and() obsoleto de Spring Security 6
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(sessionFixation -> sessionFixation.changeSessionId())
                    .maximumSessions(1)
            )
            // 3. Autorización de rutas
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos estáticos y de autenticación
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/login", "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Vistas MVC por rol
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/almacen/**").hasAnyRole("ALMACENERO", "ADMINISTRADOR")
                .requestMatchers("/produccion/**").hasAnyRole("JEFE_PRODUCCION", "ADMINISTRADOR")
                .requestMatchers("/despacho/**").hasAnyRole("ALMACENERO", "OPERARIO_ENSAMBLE", "ADMINISTRADOR")
                .requestMatchers("/calidad/**").hasAnyRole("INSPECTOR_CALIDAD", "JEFE_PRODUCCION", "ADMINISTRADOR")
                .requestMatchers("/merma/**").hasAnyRole("ALMACENERO", "OPERARIO_ENSAMBLE", "JEFE_PRODUCCION", "ADMINISTRADOR")
                .requestMatchers("/trazabilidad/**").hasAnyRole("JEFE_PRODUCCION", "ADMINISTRADOR")
                .requestMatchers("/reportes/**").hasAnyRole("JEFE_PRODUCCION", "ADMINISTRADOR")
                .requestMatchers("/home", "/dashboard").authenticated()
                
                // API REST: solo autenticados con JWT
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            // 4. Configuración de Login y Logout
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/home", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            // 5. Filtro JWT personalizado
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}