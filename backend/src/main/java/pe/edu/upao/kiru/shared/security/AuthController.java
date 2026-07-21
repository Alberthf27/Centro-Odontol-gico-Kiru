package pe.edu.upao.kiru.shared.security;

import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone solamente configuración pública de Supabase y la sesión actual.
 * Las contraseñas nunca llegan al backend de Kiru: las procesa Supabase Auth.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final String supabaseUrl;
    private final String publishableKey;

    public AuthController(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.publishable-key:}") String publishableKey
    ) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.trim();
        this.publishableKey = publishableKey == null ? "" : publishableKey.trim();
    }

    @GetMapping("/config")
    public AuthConfigResponse config() {
        return new AuthConfigResponse(
                supabaseUrl,
                publishableKey,
                !supabaseUrl.isBlank() && !publishableKey.isBlank()
        );
    }

    @GetMapping("/me")
    public AuthSessionResponse session(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return new AuthSessionResponse(
                    jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    roleFrom(jwt)
            );
        }
        // Permite revisar la interfaz en KIRU_DEMO_ENABLED sin inventar una cuenta.
        return new AuthSessionResponse(null, null, "CLIENTE");
    }

    private String roleFrom(Jwt jwt) {
        Object metadata = jwt.getClaim("app_metadata");
        if (metadata instanceof Map<?, ?> values) {
            Object role = values.get("role");
            if (role != null) {
                String normalized = role.toString().trim().toUpperCase(Locale.ROOT);
                if (normalized.equals("RECEPTIONIST")) {
                    return "RECEPCIONISTA";
                }
                if (normalized.equals("CLIENTE") || normalized.equals("RECEPCIONISTA")) {
                    return normalized;
                }
            }
        }
        // En el alcance actual los endpoints implementados son del cliente.
        return "CLIENTE";
    }
}
