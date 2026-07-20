package pe.edu.upao.kiru.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pe.edu.upao.kiru.shared.exception.BusinessRuleException;

@Component
public class AuthenticatedClientProvider {

    public String authUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessRuleException("No existe un cliente autenticado.");
        }
        return jwt.getSubject();
    }
}
