package pe.edu.upao.kiru.disponibilidades.web;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.edu.upao.kiru.disponibilidades.domain.FranjaHoraria;

public record FranjaHorariaResponse(
        String idFranja,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String idOdontologo,
        boolean disponible
) {
    public static FranjaHorariaResponse from(FranjaHoraria franja) {
        return new FranjaHorariaResponse(
                franja.getIdFranja(),
                franja.getFecha(),
                franja.getHoraInicio(),
                franja.getHoraFin(),
                franja.getOdontologo().getIdOdontologo(),
                franja.isDisponible()
        );
    }
}
