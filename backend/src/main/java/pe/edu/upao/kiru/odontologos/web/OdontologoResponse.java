package pe.edu.upao.kiru.odontologos.web;

import pe.edu.upao.kiru.odontologos.domain.Odontologo;

public record OdontologoResponse(
        String idOdontologo,
        String nombreCompleto,
        String especialidad
) {
    public static OdontologoResponse from(Odontologo odontologo) {
        return new OdontologoResponse(
                odontologo.getIdOdontologo(),
                odontologo.getNombreCompleto(),
                odontologo.getEspecialidad()
        );
    }
}
