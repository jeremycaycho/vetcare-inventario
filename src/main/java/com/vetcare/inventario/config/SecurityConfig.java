package com.vetcare.inventario.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, JwtAuthenticationConverter convertidor) throws Exception {
        String[] catalogos = {"/api/categorias/**", "/api/proveedores/**", "/api/productos/**"};

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/ping").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/movimientos/entradas")
                    .hasAnyRole("ADMINISTRADOR", "ENCARGADO")
                .requestMatchers(HttpMethod.POST, "/api/movimientos/salidas")
                    .hasAnyRole("ADMINISTRADOR", "ENCARGADO", "VENTAS")
                .requestMatchers(HttpMethod.GET, "/api/movimientos/**")
                    .hasAnyRole("ADMINISTRADOR", "ENCARGADO", "GERENTE")
                .requestMatchers(HttpMethod.GET, "/api/reportes/**")
                    .hasAnyRole("ADMINISTRADOR", "GERENTE")
                .requestMatchers(HttpMethod.POST, catalogos).hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, catalogos).hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, catalogos).hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidor))
                .authenticationEntryPoint((request, response, ex) -> escribirError(response,
                        HttpStatus.UNAUTHORIZED,
                        "Falta el token de acceso o no es válido", request.getRequestURI()))
                .accessDeniedHandler((request, response, ex) -> escribirError(response,
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para realizar esta acción", request.getRequestURI())));

        return http.build();
    }

    /** Lee el claim "rol" del token y le antepone ROLE_ para formar la autoridad. */
    @Bean
    JwtAuthenticationConverter convertidorJwt() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("rol");
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(autoridades);
        return convertidor;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void escribirError(HttpServletResponse respuesta, HttpStatus estado,
                               String mensaje, String ruta) throws IOException {
        // Escribo con comillas simples y las convierto, para no llenar el texto de barras
        String json = ("{'timestamp':'" + LocalDateTime.now() + "','status':" + estado.value()
                + ",'error':'" + estado.getReasonPhrase() + "','mensaje':'" + mensaje
                + "','ruta':'" + ruta + "','detalles':{}}").replace('\'', '"');
        respuesta.setStatus(estado.value());
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write(json);
    }
}
