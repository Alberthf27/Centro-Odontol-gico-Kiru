package pe.edu.upao.kiru.shared.security;

public record AuthSessionResponse(
        String userId,
        String email,
        String role
) {
}
