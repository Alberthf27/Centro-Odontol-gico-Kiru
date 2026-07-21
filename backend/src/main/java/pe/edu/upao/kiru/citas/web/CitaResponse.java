package pe.edu.upao.kiru.citas.web;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.citas.domain.EstadoCita;

public record CitaResponse(
        String nroCita,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoCita estado,
        String tipoCita,
        String tratamiento,
        String sesion,
        String idOdontologo,
        String nombreOdontologo
) {
    public static CitaResponse from(Cita cita) {
        return new CitaResponse(
                cita.getNroCita(),
                cita.getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                cita.getEstado(),
                cita.getTipoCita().name(),
                cita.getTratamiento() == null ? null : cita.getTratamiento().getNombre(),
                cita.getSesion() == null ? null : cita.getSesion().getNombreTipo(),
                cita.getOdontologo().getIdOdontologo(),
                cita.getOdontologo().getNombreCompleto()
        );
    }
}
