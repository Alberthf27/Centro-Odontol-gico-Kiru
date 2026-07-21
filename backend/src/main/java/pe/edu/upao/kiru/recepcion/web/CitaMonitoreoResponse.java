package pe.edu.upao.kiru.recepcion.web;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.edu.upao.kiru.citas.domain.Cita;
import pe.edu.upao.kiru.citas.domain.EstadoCita;

/**
 * Fila de la tabla "Citas médicas" del monitor de recepción: incluye los datos
 * del cliente para operar la cita sin una segunda consulta.
 */
public record CitaMonitoreoResponse(
        String nroCita,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoCita estado,
        String tipoCita,
        String tratamiento,
        String sesion,
        String idOdontologo,
        String nombreOdontologo,
        boolean asistenciaPaciente,
        String idCliente,
        String dniCliente,
        String nombresCliente,
        String apellidosCliente
) {
    public static CitaMonitoreoResponse from(Cita cita) {
        return new CitaMonitoreoResponse(
                cita.getNroCita(),
                cita.getFecha(),
                cita.getHoraInicio(),
                cita.getHoraFin(),
                cita.getEstado(),
                cita.getTipoCita().name(),
                cita.getTratamiento() == null ? null : cita.getTratamiento().getNombre(),
                cita.getSesion() == null ? null : cita.getSesion().getNombreTipo(),
                cita.getOdontologo().getIdOdontologo(),
                cita.getOdontologo().getNombreCompleto(),
                cita.isAsistenciaPaciente(),
                cita.getCliente().getIdCliente(),
                cita.getCliente().getDni(),
                cita.getCliente().getNombres(),
                cita.getCliente().getApellidos()
        );
    }
}
