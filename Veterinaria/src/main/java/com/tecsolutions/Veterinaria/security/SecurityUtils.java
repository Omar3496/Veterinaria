package com.tecsolutions.Veterinaria.security;

import com.tecsolutions.Veterinaria.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getUsername();
        }
        return authentication.getName();
    }

    public static String extractRole(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Usuario usuario && usuario.getRol() != null) {
            return usuario.getRol().getValue();
        }
        if (authentication.getAuthorities() == null) {
            return null;
        }
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            String auth = ga.getAuthority();
            if (auth != null) {
                if (auth.startsWith("ROLE_")) {
                    return auth.substring(5);
                }
                return auth;
            }
        }
        return null;
    }

    public static boolean isClienteRole(String role) {
        if (role == null) {
            return false;
        }
        return role.equalsIgnoreCase("Cliente")
                || role.equalsIgnoreCase("ROLE_Cliente")
                || role.equalsIgnoreCase("CLIENTE")
                || role.equalsIgnoreCase("ROLE_CLIENTE");
    }

    public static boolean isStaffRole(String role) {
        if (role == null) {
            return false;
        }
        String r = role.replace("ROLE_", "").toUpperCase();
        return r.equals("ADMINISTRADOR") || r.equals("ADMIN") || r.equals("VETERINARIO") || r.equals("RECEPCIONISTA");
    }

    public static boolean isAdminRole(String role) {
        if (role == null) {
            return false;
        }
        String r = role.replace("ROLE_", "").toUpperCase();
        return r.equals("ADMINISTRADOR") || r.equals("ADMIN");
    }
}
