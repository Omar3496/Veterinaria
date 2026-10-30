package com.tecsolutions.Veterinaria.security;

import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
import com.tecsolutions.Veterinaria.services.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "DELETE");

    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaInterceptor(AuditoriaService auditoriaService, UsuarioRepository usuarioRepository) {
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null || response.getStatus() >= 400) {
            return;
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (MUTATING_METHODS.contains(method) && uri.startsWith("/api/")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Usuario usuario = null;
                if (auth.getPrincipal() instanceof Usuario u) {
                    usuario = u;
                } else if (auth.getName() != null) {
                    usuario = usuarioRepository.findByUsername(auth.getName()).orElse(null);
                }

                if (usuario != null) {
                    try {
                        auditoriaService.registrarAccion(usuario, method, uri, null, "Ejecución desde middleware");
                    } catch (Exception ignored) {
                        // Avoid failing the response if audit writing has a transient issue
                    }
                }
            }
        }
    }
}
