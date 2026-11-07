package com.vetcare.inventario.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetcare.inventario.dto.LoginRequest;
import com.vetcare.inventario.dto.LoginResponse;
import com.vetcare.inventario.entity.Usuario;
import com.vetcare.inventario.exception.CredencialesInvalidasException;
import com.vetcare.inventario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-minutes}")
    private long minutosExpiracion;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username().trim())
                .filter(Usuario::isActivo)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));

        String rol = usuario.getRol().getNombre();
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("vetcare-inventario")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES))
                .subject(usuario.getUsername())
                .claim("rol", rol)
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();

        return new LoginResponse(token, "Bearer", minutosExpiracion * 60, usuario.getUsername(), rol);
    }
}
