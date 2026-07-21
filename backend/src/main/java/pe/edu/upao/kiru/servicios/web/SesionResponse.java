package pe.edu.upao.kiru.servicios.web;

import pe.edu.upao.kiru.servicios.domain.Sesion;

public record SesionResponse(
        String idSesion,
        String nombreTipo,
        int orden,
        String descripcion
) {
    static SesionResponse from(Sesion sesion) {
        return new SesionResponse(
                sesion.getIdSesion(),
                sesion.getNombreTipo(),
                sesion.getOrden(),
                sesion.getDescripcion()
        );
    }
}
