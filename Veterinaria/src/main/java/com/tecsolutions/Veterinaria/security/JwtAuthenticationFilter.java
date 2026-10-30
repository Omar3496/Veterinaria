package com.tecsolutions.Veterinaria.security;

import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        if (Boolean.TRUE.equals(usuario.getIsActive())) {
                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getValue()),
                                    new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                            );
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(usuario, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
