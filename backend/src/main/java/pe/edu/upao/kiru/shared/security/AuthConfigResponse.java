package pe.edu.upao.kiru.shared.security;

/** Configuración pública necesaria para que el navegador inicie sesión con Supabase Auth. */
public record AuthConfigResponse(
        String supabaseUrl,
        String publishableKey,
        boolean configured
) {
}
