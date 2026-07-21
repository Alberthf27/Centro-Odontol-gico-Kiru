package pe.edu.upao.kiru.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;

@Component
public class AuthenticatedClientProvider {

    private final boolean demoEnabled;
    private final String demoAuthUserId;

    public AuthenticatedClientProvider(
            @Value("${app.demo.enabled:false}") boolean demoEnabled,
            @Value("${app.demo.auth-user-id}") String demoAuthUserId
    ) {
        this.demoEnabled = demoEnabled;
        this.demoAuthUserId = demoAuthUserId;
    }

    public String authUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (demoEnabled) {
            return demoAuthUserId;
        }
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new BusinessRuleException("No existe un cliente autenticado.");
        }
        throw new BusinessRuleException("No existe un cliente autenticado.");
    }
}
