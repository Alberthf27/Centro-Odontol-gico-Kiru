package pe.edu.upao.kiru.servicios.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import pe.edu.upao.kiru.servicios.domain.PlanTratamiento;

public record PlanTratamientoResponse(
        String idPlan,
        String descripcion,
        LocalDate fechaInicio,
        BigDecimal progreso,
        String idTratamiento,
        String tratamiento,
        BigDecimal precio,
        String idOdontologo,
        String odontologo,
        List<SesionResponse> sesionesPendientes
) {
    public static PlanTratamientoResponse from(PlanTratamiento plan) {
        return new PlanTratamientoResponse(
                plan.getIdPlan(),
                plan.getDescripcion(),
                plan.getFechaInicio(),
                plan.getProgreso(),
                plan.getTratamiento().getIdTratamiento(),
                plan.getTratamiento().getNombre(),
                plan.getTratamiento().getPrecio(),
                plan.getOdontologo().getIdOdontologo(),
                plan.getOdontologo().getNombreCompleto(),
                plan.getSesionesPendientes().stream().map(SesionResponse::from).toList()
        );
    }
}
